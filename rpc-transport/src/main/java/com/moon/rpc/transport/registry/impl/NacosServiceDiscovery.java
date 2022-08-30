package com.moon.rpc.transport.registry.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.moon.rpc.transport.loadbalance.LoadBalance;
import com.moon.rpc.transport.registry.InstanceNode;
import com.moon.rpc.transport.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
     * @return
     */
    @Override
    public InstanceNode select(String serviceName, Set<InstanceNode> invoked) {
        // TODO 重选前应该再次验证服务器是否可用
        // 获取服务器列表
        List<Instance> instanceList = null;
        try {
            instanceList = namingService.getAllInstances(serviceName);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
        if (instanceList.size() == 0) {
            throw new RuntimeException("找不到对应服务");
        }
        List<InstanceNode> instanceNodes = instanceList.stream().map(e -> new InstanceNode(serviceName, e.getIp(), e.getPort())).collect(Collectors.toList());
        return doSelect(instanceNodes, invoked);
    }

    private InstanceNode doSelect(List<InstanceNode> instanceNodes, Set<InstanceNode> invoked) {
        if (instanceNodes.size() == 1) {
            return instanceNodes.get(0);
        }
        InstanceNode instanceNode = loadBalance.select(instanceNodes);
        if (invoked.contains(instanceNode)) {
            // 重新进行选择
            // * Reselect, use invokers not in `selected` first, if all invokers are in `selected`,
            // * just pick an available one using loadbalance policy.
            instanceNode = reselect(instanceNodes, invoked, loadBalance);
        }
        return instanceNode;
    }

    private InstanceNode reselect(List<InstanceNode> instanceNodes, Set<InstanceNode> invoked, LoadBalance loadBalance) {
        // 1. Try picking some invokers not in `selected`.
        List<InstanceNode> reselectInstanceNodes = new ArrayList<>();
        for (InstanceNode instanceNode : instanceNodes) {
            if (invoked.contains(instanceNode)) continue;
            reselectInstanceNodes.add(instanceNode);
        }

        // 2. 重选列表不为空
        if (!reselectInstanceNodes.isEmpty()) {
            return loadBalance.select(reselectInstanceNodes);
        }

        // 3. 重选列表为空，则只能从已经选过的列表中继续选
        if (invoked != null) {
            for (InstanceNode instanceNode : invoked) {
                reselectInstanceNodes.add(instanceNode);
            }
        }

        // 4. If reselectInvokers is not empty after re-check.
        //    Pick an available invoker using loadBalance policy
        if (!reselectInstanceNodes.isEmpty()) {
            return loadBalance.select(reselectInstanceNodes);
        }

        // 5. No invoker match, return null.
        return null;


    }

}
