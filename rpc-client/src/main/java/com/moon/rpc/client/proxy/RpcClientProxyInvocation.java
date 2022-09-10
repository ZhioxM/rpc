package com.moon.rpc.client.proxy;

import com.alibaba.nacos.api.exception.NacosException;
import com.moon.rpc.client.annotation.RpcReference;
import com.moon.rpc.client.async.InvokeCompletableFuture;
import com.moon.rpc.client.async.RpcContext;
import com.moon.rpc.client.factory.LocalRpcResponseFactory;
import com.moon.rpc.client.factory.RemoteChannelFactory;
import com.moon.rpc.client.generic.RpcGenericService;
import com.moon.rpc.client.transport.DefaultResponseFuture;
import com.moon.rpc.client.transport.ResponseFuture;
import com.moon.rpc.transport.dto.RpcRequest;
import com.moon.rpc.transport.dto.RpcResponse;
import com.moon.rpc.transport.exception.RpcException;
import com.moon.rpc.transport.protocol.SequenceIdGenerator;
import com.moon.rpc.transport.registry.InstanceNode;
import com.moon.rpc.transport.registry.ServiceDiscovery;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
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

        RpcRequest rpcRequest = new RpcRequest(
                sequenceId,
                clasName,
                version,
                methodName,
                parameterTypes,
                parameters,
                timeout
        );

        // 计算最多的发送次数
        int len = rpcReference.retries() + 1;
        // 保存已经调用的InstanceNode
        Set<InstanceNode> selected = new HashSet<>(len);
        // 3. 异常重试
        // TODO 异步的超时重试可能有问题
        for (int i = 0; i < len; i++) {
            // 选择一个Instance(服务器地址)
            if (i != 0) {
                // 发生了异常重试，超时时间缩小为一半，防止由于多次重试导致调用方等待时间延长
                rpcRequest.setTimeout(rpcRequest.getTimeout() / 2);
            }
            InstanceNode instanceNode = select(rpcRequest.getInterfaceName(), selected);
            selected.add(instanceNode);
            Object result = null;
            try {
                // 4 调用服务
                result = doInvoke(rpcRequest, instanceNode);
                // 5 返回响应结果
                return result;
            } catch (RpcException e) {
                // 业务异常不需要重试，直接抛出; 这次请求已经超时了，不需要重试，直接抛出
                if (e.isBiz() || e.isTimeout()) {
                    throw e;
                }
            } catch (Throwable e) {
                throw new RpcException(e.getMessage(), e);
            }
        }
        // 重试还是失败了
        throw new RpcException(RpcException.UNKNOWN_EXCEPTION, "Fail to call method " + method.getName() + ". Tried " + len + " times in the service");
    }

    // 服务发现
    private InstanceNode select(String interfaceName, Set<InstanceNode> invoked) throws NacosException {
        // 根据配置的负载均衡策略，获取服务器的地址
        return serviceDiscovery.select(interfaceName, invoked);
    }

    private Object doInvoke(RpcRequest rpcRequest, InstanceNode instanceNode) {
        // 2. 准备一个ResponseFuture 用于接受异步结果
        ResponseFuture<RpcResponse> future = new DefaultResponseFuture<>();
        LocalRpcResponseFactory.add(rpcRequest.getSequenceId(), future);
        // 3. 进行网络通信，发起RPC调用，将消息对象发送出去
        // 3.1 获取客户端与该服务器的Channel
        Channel channel = RemoteChannelFactory.get(instanceNode);
        // 3.2 发送消息
        ChannelFuture channelFuture = channel.writeAndFlush(rpcRequest);
        // 3.3 等待消息发送完成
        try {
            channelFuture.sync();
        } catch (InterruptedException e) {
            throw new RpcException(RpcException.NETWORK_EXCEPTION, "Channel send message is interruted");
        }
        RpcResponse rpcResponse = null;
        if (channelFuture.isSuccess()) {
            log.info("客户端发送消息成功");
            // 4. 判断调用类型是否为同步
            if (rpcReference.async()) {
                // 5. 是否需要返回值？
                if (!rpcReference.oneWay()) {
                    // 5.1 将future存储在ThreadLocal中
                    // TODO 确认业务调用线程和这里发送线程是不是同一个线程 已经确认，这里还是用户线程
                    // ResponseFuture存储RpcResponse
                    // InvokeCompletableFuture存储RPC方法的返回值
                    InvokeCompletableFuture<?> completableFuture = new InvokeCompletableFuture<>(future);
                    RpcContext.setCompletableFuture(rpcRequest.getSequenceId(), future, completableFuture);
                }
            } else {
                // 6. 阻塞等待RPC结果
                rpcResponse = future.get(rpcReference.timeout(), TimeUnit.MILLISECONDS);
            }
        } else {
            log.error("客户端消息发送失败");
            throw new RpcException(RpcException.NETWORK_EXCEPTION, "Channel send message is failed");
        }
        // 响应结果中有异常信息，则抛出业务异常
        if (rpcResponse == null || rpcResponse.getException() != null) {
            // rpcResponse肯定是非空的，因为如果为空，在get的时候就抛出运行时异常了
            // TODO 服务端返回的异常需要特殊处理，如果是判断是业务异常，还是因为压力太大导致的异常
            throw new RpcException(RpcException.BIZ_EXCEPTION, rpcResponse == null ? "调用异常" : rpcResponse.getException().getMessage());
        }
        return rpcResponse.getReturnValue();
    }
}
