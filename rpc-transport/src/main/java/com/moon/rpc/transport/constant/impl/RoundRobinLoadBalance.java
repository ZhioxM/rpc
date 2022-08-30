package com.moon.rpc.transport.constant.impl;

import com.moon.rpc.transport.loadbalance.LoadBalance;
import com.moon.rpc.transport.registry.InstanceNode;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 轮询负载均衡算法
 *
 * @author chenlei
 */
public class RoundRobinLoadBalance implements LoadBalance {

    /**
     * 使用原子计数器做轮循（注意，按理说应该每一个Service都应该有自己的原子计数器，不能跟其他的一起混用）
     * dubbo是每一个service+method都有自己的原子计数器，这里简单一点
     */
    private ConcurrentHashMap<String, WeightedRobinRobin> serviceWeightedMap = new ConcurrentHashMap<>();

    private class WeightedRobinRobin {
        private int weight;
        private AtomicLong current;
        private long lastUpdate;

        public WeightedRobinRobin() {
            // 初始化为为一个随机值
            current = new AtomicLong(new Random().nextInt());
        }
    }


    /**
     * 轮询获取实例
     *
     * @param list
     * @return
     */
    @Override
    public InstanceNode select(List<InstanceNode> list, String serviceKey) {
        WeightedRobinRobin weightedRobinRobin = serviceWeightedMap.computeIfAbsent(serviceKey, e -> new WeightedRobinRobin());
        int index = (int) weightedRobinRobin.current.getAndIncrement() % list.size();
        return list.get(index);
    }
}
