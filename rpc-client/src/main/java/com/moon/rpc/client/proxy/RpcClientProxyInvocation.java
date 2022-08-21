package com.moon.rpc.client.proxy;

import com.alibaba.nacos.api.exception.NacosException;
import com.moon.rpc.client.annotation.RpcReference;
import com.moon.rpc.client.exception.TimeoutException;
import com.moon.rpc.client.exception.TransactionException;
import com.moon.rpc.client.exception.TransportException;
import com.moon.rpc.client.factory.LocalRpcResponseFactory;
import com.moon.rpc.client.factory.RemoteChannelFactory;
import com.moon.rpc.client.transport.DefaultResponseFuture;
import com.moon.rpc.client.transport.ResponseFuture;
import com.moon.rpc.transport.dto.RpcRequest;
import com.moon.rpc.transport.dto.RpcResponse;
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
        // 保存已经调用的InstanceNode
        Set<InstanceNode> selected = new HashSet<>(len);
        for (int i = 0; i < len; i++) {
            // 选择一个Instance(服务器地址)
            InstanceNode instanceNode = select(rpcRequest.getInterfaceName(), selected);
            RpcResponse rpcResponse = null;
            try {
                rpcResponse = doInvoke(rpcRequest, instanceNode);
            } catch (TransportException e) {
                // 客户端发送消息失败，也进行充实
                selected.add(instanceNode);
                continue;
            }
            // 4 处理响应结果
            if (rpcResponse == null) {
                log.error("请求超时");
                // TODO 超时后是不是要把LocalResponseFutureFactory中将这次请求对应的future删除掉，不然之后重试拿到的结果结果可能是之前的请求的响应。但是如此一来，之前的那个sequenceID是不是得重新生成，因为这样就不能标识一次请求和响应、
                // 超时后，我们无法阻止客户端channelRead触发，如果不生成新的id，那么即使移除了future，之前的请求超时响应回来后还是会非后面添加进去的future错误设置结果，因为他们的sequenceID是一样的
                selected.add(instanceNode);
            } else {
                // 业务异常（没有必要重试）
                if (rpcResponse.getException() != null) {
                    log.error("RPC调用异常");
                    throw new TransactionException(rpcResponse.getException().getMessage());
                } else {
                    return rpcResponse.getReturnValue();
                }
            }
        }
        // 重试还是失败了
        throw new TimeoutException("Fail to call method " + method.getName() + ". Tried " + len + " times in the service");
    }

    // 服务发现
    private InstanceNode select(String interfaceName, Set<InstanceNode> invoked) throws NacosException {
        // 根据配置的负载均衡策略，获取服务器的地址
        return serviceDiscovery.select(interfaceName, invoked);
    }

    private RpcResponse doInvoke(RpcRequest rpcRequest, InstanceNode instanceNode) throws NacosException, InterruptedException {
        // 2. 准备一个ResponseFuture 用于接受异步结果
        ResponseFuture<RpcResponse> future = new DefaultResponseFuture<>();
        // TODO 这里的超时是只要重试的次数内发送的所有请求中有一个能拿到结果就行了
        LocalRpcResponseFactory.add(rpcRequest.getSequenceId(), future);

        // 3. 进行网络通信，发起RPC调用，将消息对象发送出去
        // 3.1 获取客户端与该服务器的Channel
        Channel channel = RemoteChannelFactory.get(instanceNode);
        // 3.2 发送消息
        ChannelFuture channelFuture = channel.writeAndFlush(rpcRequest);
        // 3.3 等待消息发送完成
        channelFuture.sync();
        RpcResponse rpcResponse = null;
        if (channelFuture.isSuccess()) {
            log.info("客户端发送消息成功");
            // 4. 异步等待 超时 阻塞等待RPC结果
            rpcResponse = future.get(rpcReference.timeout(), TimeUnit.MILLISECONDS);
        } else {
            log.error("客户端消息发送失败");
            throw new TransportException("客户端发送消息失败");
        }
        return rpcResponse;
    }
}
