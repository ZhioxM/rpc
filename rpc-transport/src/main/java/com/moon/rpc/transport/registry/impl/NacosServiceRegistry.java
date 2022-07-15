package com.moon.rpc.transport.registry.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.moon.rpc.transport.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
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
     *
     * @param serviceName
     * @param inetSocketAddress
     */
    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
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
    public List<Instance> getAllInstance(String serverName) throws NacosException {
        return namingService.getAllInstances(serverName);
    }
}
