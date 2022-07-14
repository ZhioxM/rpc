package com.moon.rpc.client.transport;

import com.alibaba.nacos.api.exception.NacosException;
import com.moon.rpc.client.factory.RemoteChannelFactory;
import com.moon.rpc.transport.dto.RpcRequest;
import com.moon.rpc.transport.loadbalance.LoadBalancer;
import com.moon.rpc.transport.loadbalance.impl.RoundRobinRule;
import com.moon.rpc.transport.registry.ServiceDiscovery;
import com.moon.rpc.transport.registry.impl.NacosServiceDiscovery;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class RpcClient {
    /**
     * 服务发现表
     */
    private final ServiceDiscovery serviceDiscovery;

    /**
     * 使用无参构造，则默认使用轮询的方式进行服务发现
     */
    public RpcClient() {
        this.serviceDiscovery = new NacosServiceDiscovery(new RoundRobinRule());
    }

    /**
     * @param loadBalancer 负载均衡策略
     */
    public RpcClient(LoadBalancer loadBalancer) {
        this.serviceDiscovery = new NacosServiceDiscovery(loadBalancer);
    }

    /**
     * 发送Rpc请求消息
     *
     * @param msg
     */
    public void sendRpcRequest(RpcRequest msg) throws NacosException {
        // 根据配置的负载均衡策略，获取服务器的地址
        InetSocketAddress serviceAddr = serviceDiscovery.selectService(msg.getApiName());
        // 获取客户端与该服务器的Channel
        Channel channel = RemoteChannelFactory.get(serviceAddr);
        if (channel == null || !channel.isActive() || !channel.isRegistered()) {
            log.debug("通道已关闭, RPC调用失败");
            // TODO 通道关闭的话应该重新连接或者选择其他通道才对啊，怎么能调用失败呢
            return;
        }
        channel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端发送消息成功");
                // TODO 应该把Future的代码放到这里去，或者返回一个值（但是返回一个值的话就变成同步了）
            } else {
                log.info("客户端消息发送失败");
            }
        });
    }
}
