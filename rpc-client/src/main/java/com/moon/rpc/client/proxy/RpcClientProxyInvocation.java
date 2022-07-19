package com.moon.rpc.client.proxy;

import com.alibaba.nacos.api.exception.NacosException;
import com.moon.rpc.client.annotation.RpcReference;
import com.moon.rpc.client.exception.RemotingException;
import com.moon.rpc.client.exception.TimeoutException;
import com.moon.rpc.client.factory.LocalRpcResponseFactory;
import com.moon.rpc.client.factory.RemoteChannelFactory;
import com.moon.rpc.client.transport.DefaultResponseFuture;
import com.moon.rpc.client.transport.ResponseFuture;
import com.moon.rpc.transport.dto.RpcRequest;
import com.moon.rpc.transport.dto.RpcResponse;
import com.moon.rpc.transport.protocol.SequenceIdGenerator;
import com.moon.rpc.transport.registry.Invoker;
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
        RpcRequest rpcRequest = new RpcRequest(
                sequenceId,
                clazz.getName(),
                rpcReference.version(),
                method.getName(),
                method.getParameterTypes(),
                args,
                rpcReference.timeout()
        );
        // 计算最多的发送次数
        int len = rpcReference.retries() + 1;
        // 保存已经调用的invoker
        Set<Invoker> invoked = new HashSet<>(10);
        boolean isSuccess = false;
        for (int i = 0; i < len; i++) {
            // 选择一个invoker(服务器地址)
            Invoker invoker = select(rpcRequest.getInterfaceName(), invoked);
            try {
                Object result = doInvoke(rpcRequest, invoker);
                isSuccess = true;
                return result;
            } catch (TimeoutException ignored) {
                // 只捕获非业务异常Timeout
                // TODO 优化异常体系
            } finally {
                // 没有成功（非业务异常），则把这次调用的服务器地址保存起来
                if (!isSuccess) {
                    invoked.add(invoker);
                }
            }
        }
        // 重试还是失败了
        throw new RemotingException("Fail to call method " + method.getName() + ". Tried " + len + " times in the service");
    }

    // 服务发现
    private Invoker select(String interfaceName, Set<Invoker> invoked) throws NacosException {
        // 根据配置的负载均衡策略，获取服务器的地址
        return serviceDiscovery.select(interfaceName, invoked);
    }

    private Object doInvoke(RpcRequest rpcRequest, Invoker invoker) throws NacosException, InterruptedException {
        // 2. 准备一个ResponseFuture 用于接受异步结果
        ResponseFuture<RpcResponse> future = new DefaultResponseFuture<>();
        // TODO 这里的超时是只要重试的次数内发送的所有请求中有一个能拿到结果就行了
        LocalRpcResponseFactory.add(rpcRequest.getSequenceId(), future);

        // 3. 进行网络通信，发起RPC调用，将消息对象发送出去
        // 3.1 获取客户端与该服务器的Channel
        Channel channel = RemoteChannelFactory.get(invoker);
        // 3.2 发送消息
        ChannelFuture channelFuture = channel.writeAndFlush(rpcRequest);
        channelFuture.sync();
        if (channelFuture.isSuccess()) {
            log.info("客户端发送消息成功");
            // 4. 异步等待 超时 阻塞等待RPC结果
            RpcResponse rpcResponse = null;
            rpcResponse = future.get(rpcReference.timeout(), TimeUnit.MILLISECONDS);

            // 5. 处理响应结果
            // 非业务异常
            if (rpcResponse == null) {
                log.error("请求超时");
                throw new TimeoutException("RPC调用超时，超时时间:" + rpcReference.timeout());
            }
            // 业务异常（没有必要重试）
            if (rpcResponse.getException() != null) {
                log.error("RPC调用异常");
                // throw new RemotingException(rpcResponse.getException().getMessage());
                // 不抛出异常了，改为返回异常信息
                return rpcResponse.getException().getMessage();
            } else {
                return rpcResponse.getReturnValue();
            }
        } else {
            log.error("客户端消息发送失败");
            throw new RemotingException("客户端发送消息失败");
        }
    }
}
