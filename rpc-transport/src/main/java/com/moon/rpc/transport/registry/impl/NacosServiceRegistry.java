package com.moon.rpc.transport.registry.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.moon.rpc.transport.registry.InstanceNode;
import com.moon.rpc.transport.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.moon.rpc.transport.constant.Constants.*;

/**
 * nacos注册
 * 供服务端使用
 *
 * @author mzx
 */
@Slf4j
public class NacosServiceRegistry implements ServiceRegistry {

    private NamingService namingService;
    private Integer warmUp;

    public NacosServiceRegistry(String registryAddr, Integer warmUp) {
        this.warmUp = warmUp;
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
    public void register(String serviceName, String host, int port, String groupName, int weight) {
        try {
            Instance instance = new Instance();
            instance.setWeight((double) weight);
            instance.setIp(host);
            instance.setPort(port);
            HashMap<String, String> metaDate = new HashMap<>();
            // 将启动时间和预热时间存入注册中心中
            metaDate.put(REMOTE_TIMESTAMP_KEY, System.currentTimeMillis() + "");
            metaDate.put(WARM_UP_KEY, warmUp == null ? String.valueOf(DEFAULT_WARM_UP) : String.valueOf(warmUp));
            instance.setMetadata(metaDate);
            namingService.registerInstance(serviceName, groupName, instance);
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
            InstanceNode instanceNode = new InstanceNode(instance.getIp(), instance.getPort(), (int) instance.getWeight(), instance.getMetadata());
            instanceNodes.add(instanceNode);
        }
        return instanceNodes;
    }
}
