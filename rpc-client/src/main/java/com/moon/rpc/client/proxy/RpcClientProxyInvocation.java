package com.moon.rpc.client.proxy;

import com.alibaba.nacos.api.exception.NacosException;
import com.moon.rpc.client.annotation.RpcReference;
import com.moon.rpc.client.async.RpcContext;
import com.moon.rpc.client.factory.LocalRpcResponseFactory;
import com.moon.rpc.client.generic.RpcGenericService;
import com.moon.rpc.transport.dto.RpcRequest;
import com.moon.rpc.transport.dto.RpcResponse;
import com.moon.rpc.transport.exception.RpcException;
import com.moon.rpc.transport.protocol.SequenceIdGenerator;
import com.moon.rpc.transport.registry.InstanceNode;
import com.moon.rpc.transport.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author mzx
 * @date 2022/7/13 19:22
 */
@Slf4j
public class RpcClientProxyInvocation implements InvocationHandler {
    private Class<?> clazz;
    private RpcReference rpcReference;

    private ServiceDiscovery serviceDiscovery;

    public RpcClientProxyInvocation(Class<?> clazz, RpcReference rpcReference, ServiceDiscovery serviceDiscovery) {
        this.clazz = clazz;
        this.rpcReference = rpcReference;
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 将方法调用转换为消息对象
        int sequenceId = SequenceIdGenerator.nextId();
        String clasName = clazz.getName();
        String version = rpcReference.version();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] parameters = args;
        long timeout = rpcReference.timeout();

        // 2. filter for generic
        if (clazz.getName().equals(RpcGenericService.class.getName()) && methodName.equals("invoke")) {
            clasName = (String) args[0];
            methodName = (String) args[1];
            parameterTypes = (Class<?>[]) args[2];
            parameters = (Object[]) args[3];
        }
        // 3. 构造请求
        RpcRequest rpcRequest = new RpcRequest(
                sequenceId,
                clasName,
                version,
                methodName,
                parameterTypes,
                parameters,
                timeout
        );
        // 4. 判断接口返回值是什么，如果接口返回值是CompleteFuture类，则认为是异步的，那么接口就需要同时提供异步和同步两种方法
        boolean isReturnFuture = CompletableFuture.class == method.getReturnType();
        boolean isAsync = rpcReference.async();
        boolean isOneWay = rpcReference.oneWay();
        if (isOneWay) {
            return doOneWay(rpcRequest);
        } else {
            return isReturnFuture ? doAsync(rpcRequest) : (isAsync ? doContextAsync(rpcRequest) : doSync(rpcRequest));
        }
    }

    /**
     * 返回值与接口返回值一致
     *
     * @param rpcRequest
     * @return
     */
    private Object doSync(RpcRequest rpcRequest) {
        try {
            CompletableFuture<RpcResponse> future = doInvoke(rpcRequest);
            // 阻塞
            RpcResponse rpcResponse = future.get(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
            if (rpcResponse.isException()) {
                throw rpcResponse.getException();
            }
            return rpcResponse.getReturnValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 异步调用
     * 方法返回值是Future类
     *
     * @param rpcRequest
     * @return
     */
    private CompletableFuture<Object> doAsync(RpcRequest rpcRequest) {
        CompletableFuture<Object> response = new CompletableFuture<>();
        try {
            CompletableFuture<RpcResponse> future = doInvoke(rpcRequest);
            // 不阻塞
            future.whenComplete((res, err) -> {
                Throwable throwable = err == null ? res.getException() : err;
                if (throwable != null) {
                    response.completeExceptionally(err);
                } else {
                    response.complete(res.getReturnValue());
                }
            });
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    /**
     * 上下文异步调用
     * 方法返回值是正常的返回值，但是是异步调用的，future类从上下文获取
     *
     * @return
     */
    private Object doContextAsync(RpcRequest rpcRequest) {
        // 将异步future存入异步上下文
        RpcContext.getContext().setFuture(doAsync(rpcRequest));
        // 返回空值
        return null;
    }

    private Object doOneWay(RpcRequest rpcRequest) {
        doSync(rpcRequest);
        return null;
    }

    // 服务发现
    private InstanceNode select(String interfaceName, Set<InstanceNode> invoked) throws NacosException {
        // 根据配置的负载均衡策略，获取服务器的地址
        return serviceDiscovery.select(interfaceName, invoked);
    }

    private CompletableFuture<RpcResponse> doInvoke(RpcRequest rpcRequest) throws NacosException {
        CompletableFuture<RpcResponse> future = new CompletableFuture<>();
        CompletableFuture<RpcResponse> result = new CompletableFuture<>();
        retry(rpcRequest, future, result, 0);
        return future;
    }

    private void retry(RpcRequest rpcRequest, CompletableFuture<RpcResponse> future, CompletableFuture<RpcResponse> result, int retries) {
        // 将Future类存入缓存中
        LocalRpcResponseFactory.add(rpcRequest.getSequenceId(), result);
        // 异步处理结果
        // TODO jdk8的Completable不支持异步超时等待机制，jdk9后才实现。自己封装一个，参考https://blog.csdn.net/weixin_40118044/article/details/121687726
        result.whenComplete((res, err) -> {
            Throwable e = err == null ? res.getException() : err;
            if (err == null) {
                future.complete(res);
            } else if (rpcRequest.isTimeOut()) {
                future.completeExceptionally(new RpcException(RpcException.TIMEOUT_EXCEPTION, "调用超时了"));
            } else if (retries > rpcReference.retries()) {
                future.completeExceptionally(new RpcException(RpcException.UNKNOWN_EXCEPTION, "重传了" + retries + "次，还是失败了，失败原因：" + err.getMessage()));
            } else {
                retry(rpcRequest, future, result, retries + 1);
            }
        });
    }
}
