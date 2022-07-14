package com.moon.rpc;

import com.moon.rpc.api.service.HelloService;
import com.moon.rpc.client.proxy.RpcClientProxy;
import com.moon.rpc.client.transport.RpcClient;

/**
 * @author mzx
 * @date 2022/7/14 15:09
 */
public class RpcConsumer {
    public static void main(String[] args) {
        // 通过代理的方式封装了netty通信的过程，使得用户只关注于框架的使用
        RpcClient rpcClient = new RpcClient();
        // 封装通信过
        // 客户端代理对象，通过动态代理封装了远程通信的过程
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        // 获取服务的服务存根
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        //OrderService orderService = rpcClientProxy.getProxy(OrderService.class);
        // 通过代理对象发起RPC请求
        System.out.println(helloService.sayHello("zhangsan"));
    }
}
