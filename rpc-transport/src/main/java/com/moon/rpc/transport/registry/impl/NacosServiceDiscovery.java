package com.moon.rpc.transport.registry.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.moon.rpc.transport.loadbalance.LoadBalance;
import com.moon.rpc.transport.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 服务发现
 * 供客户端使用
 *
 * @author mzx
 */
@Slf4j
public class NacosServiceDiscovery implements ServiceDiscovery {

    private final LoadBalance loadBalance;
    private final NamingService namingService;

    /**
     * @param loadBalance 负载均衡算法
     */
    public NacosServiceDiscovery(String registryAddr, LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
        try {
            namingService = NamingFactory.createNamingService(registryAddr);
        } catch (NacosException e) {
            throw new RuntimeException("连接到Nacos时发生错误");
        }
    }


    /**
     * 使用负载均衡策略
     * 根据服务名找到服务地址
     *
     * @param serviceName
     * @return
     */
    @Override
    public InetSocketAddress selectService(String serviceName) throws NacosException {
        // 获取服务器列表
        List<Instance> instanceList = namingService.getAllInstances(serviceName);
        if (instanceList.size() == 0) {
            throw new RuntimeException("找不到对应服务");
        }
        // 根据负载均衡策略选择服务器
        Instance instance = loadBalance.getInstance(instanceList);
        log.debug("客户端选择的服务提供者是{} : {}", instance.getIp(), instance.getPort());
        return new InetSocketAddress(instance.getIp(), instance.getPort());
    }

}
