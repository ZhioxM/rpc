package com.moon.rpc.client.transport;

import com.alibaba.nacos.api.exception.NacosException;
import com.moon.rpc.transport.dto.RpcRequest;
import com.moon.rpc.transport.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Deprecated
public class RpcClient {

    /**
     * 服务发现表
     */
    private ServiceDiscovery serviceDiscovery;

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * 发送Rpc请求消息
     */
    public void sendRpcRequest(RpcRequest msg) throws NacosException {
        //// 根据配置的负载均衡策略，获取服务器的地址
        //InetSocketAddress serviceAddr = serviceDiscovery.select(msg.getInterfaceName());
        //// 获取客户端与该服务器的Channel
        //Channel channel = RemoteChannelFactory.get(serviceAddr);
        //if (channel == null || !channel.isActive() || !channel.isRegistered()) {
        //    log.debug("通道已关闭, RPC调用失败");
        //    // TODO 通道关闭的话应该重新连接或者选择其他通道才对啊，怎么能调用失败呢
        //    return;
        //}
        //channel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
        //    if (future.isSuccess()) {
        //        log.info("客户端发送消息成功");
        //        // TODO 应该把Future的代码放到这里去，或者返回一个值（但是返回一个值的话就变成同步了）
        //    } else {
        //        log.info("客户端消息发送失败");
        //    }
        //});
    }
}
