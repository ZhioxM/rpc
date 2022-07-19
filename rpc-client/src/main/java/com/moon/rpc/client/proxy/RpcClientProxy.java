package com.moon.rpc.client.proxy;

import com.moon.rpc.client.annotation.RpcReference;
import com.moon.rpc.transport.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代理客户端，将方法调用封装成RPC请求，隐藏RPC通信的细节。让使用者觉得就像在本地调用一样
 *
 * @Author: Mzx
 * @Date: 2022/6/11 21:27
 */
@Slf4j
public class RpcClientProxy {
    private ServiceDiscovery serviceDiscovery;

    public RpcClientProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * 缓存接口类的代理对象。没有必要每次调用都重新生成一个新的代理对象
     * Key : 服务接口的Class对象
     * Value：服务接口的代理对象
     */
    private Map<Class<?>, Object> clientSubCache = new ConcurrentHashMap<>();

    /**
     * 获取远程服务的代理对象
     *
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked") // 抑制unchecked警告
    public <T> T getProxy(Class<T> clazz, RpcReference rpcReference) {
        return (T) clientSubCache.computeIfAbsent(clazz, clz ->
                Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, new RpcClientProxyInvocation(clazz, rpcReference, serviceDiscovery))
        );
    }
}
