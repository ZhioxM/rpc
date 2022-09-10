package com.moon.rpc.transport.registry;

/**
 * 服务注册接口
 *
 * @author chenlei
 */
public interface ServiceRegistry {
    /**
     * 将服务的名称和地址注册进服务注册中心
     */
    void register(String serviceName, String host, int port, String groupName, int weight);

}
