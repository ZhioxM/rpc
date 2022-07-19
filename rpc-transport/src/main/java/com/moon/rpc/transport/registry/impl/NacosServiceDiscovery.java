package com.moon.rpc.transport.registry.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.moon.rpc.transport.loadbalance.LoadBalance;
import com.moon.rpc.transport.registry.Invoker;
import com.moon.rpc.transport.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    public Invoker select(String serviceName, Set<Invoker> invoked) {
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
        List<Invoker> invokers = new ArrayList<>(instanceList.size());
        for (Instance instance : instanceList) {
            invokers.add(new Invoker(serviceName, instance.getIp(), instance.getPort()));
        }
        return doSelect(invokers, invoked);

    }

    private Invoker doSelect(List<Invoker> invokers, Set<Invoker> invoked) {
        if (invokers.size() == 1) {
            return invokers.get(0);
        }
        Invoker invoker = loadBalance.select(invokers);
        if (invoked.contains(invoker)) {
            // 重新进行选择
            // * Reselect, use invokers not in `selected` first, if all invokers are in `selected`,
            // * just pick an available one using loadbalance policy.
            invoker = reselect(invokers, invoked, loadBalance);
        }
        return invoker;
    }

    private Invoker reselect(List<Invoker> invokers, Set<Invoker> invoked, LoadBalance loadBalance) {
        // 1. Try picking some invokers not in `selected`.
        List<Invoker> reselectInvokers = new ArrayList<>();
        for (Invoker invoker : invokers) {
            if (invoked.contains(invoker)) continue;
            reselectInvokers.add(invoker);
        }

        // 2. 重选列表不为空
        if (!reselectInvokers.isEmpty()) {
            return loadBalance.select(reselectInvokers);
        }

        // 3. 重选列表为空，则只能从已经选过的列表中继续选
        if (invoked != null) {
            for (Invoker invoker : invoked) {
                reselectInvokers.add(invoker);
            }
        }

        // 4. If reselectInvokers is not empty after re-check.
        //    Pick an available invoker using loadBalance policy
        if (!reselectInvokers.isEmpty()) {
            return loadBalance.select(reselectInvokers);
        }

        // 5. No invoker match, return null.
        return null;


    }

}
