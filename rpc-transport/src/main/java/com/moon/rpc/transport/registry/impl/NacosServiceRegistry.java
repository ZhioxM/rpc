package com.moon.rpc.transport.registry.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.moon.rpc.transport.registry.InstanceNode;
import com.moon.rpc.transport.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * nacos注册
 * 供服务端使用
 *
 * @author mzx
 */
@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {

    private NamingService namingService;

    public NacosServiceRegistry(String registryAddr) {
        try {
            namingService = NamingFactory.createNamingService(registryAddr);
        } catch (NacosException e) {
            log.error("nacos连接异常：{}", e.getMessage());
        }
    }

    /**
     * 服务注册
     */
    @Override
    public void register(String serviceName, String host, int port) {
        try {
            namingService.registerInstance(serviceName, host, port);
            log.debug(serviceName + " 服务成功注册到远程服务中心");
        } catch (NacosException e) {
            throw new RuntimeException("注册Nacos出现异常");
        }
    }

    /**
     * 获取当前服务名中的所有实例
     *
     * @param serverName
     * @return
     * @throws NacosException
     */
    public List<InstanceNode> getAllInstance(String serverName) throws NacosException {
        List<Instance> instances = namingService.getAllInstances(serverName);
        List<InstanceNode> instanceNodes = new ArrayList<>();
        for (Instance instance : instances) {
            InstanceNode instanceNode = new InstanceNode(serverName, instance.getIp(), instance.getPort());
            instanceNodes.add(instanceNode);
        }
        return instanceNodes;
    }
}
