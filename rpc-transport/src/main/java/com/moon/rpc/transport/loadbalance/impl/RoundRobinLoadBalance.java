package com.moon.rpc.transport.loadbalance.impl;

import com.moon.rpc.transport.registry.InstanceNode;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 轮询负载均衡算法
 *
 * @author chenlei
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    /**
     * 使用原子计数器做轮循（注意，按理说应该每一个Service都应该有自己的原子计数器，不能跟其他的一起混用）
     * dubbo是每一个service+method都有自己的原子计数器，这里简单一点
     * service+key -> Map(instance -> 计数器)
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<String, WeightedRoundRobin>> serviceWeightMap = new ConcurrentHashMap<>();
    // WeightRoundRobin的回收周期， 默认60秒
    private static final int RECYCLE_PERIOD = 60000;
    protected static class WeightedRoundRobin {
        private int weight;
        private AtomicLong current = new AtomicLong(0L);
        private long lastUpdate;

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
            current.set(0);
        }

        public long increaseCurrent() {
            return current.addAndGet(weight);
        }

        public void sel(int total) {
            current.addAndGet(-1 * total);
        }

        public long getLastUpdate() {
            return lastUpdate;
        }

        public void setLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }


    // public InstanceNode select(List<InstanceNode> list, String serviceKey) {
    //     WeightedRoundRobin weightedRoundRobin = serviceWeightMap.computeIfAbsent(serviceKey, e -> new WeightedRoundRobin());
    //     int index = (int) weightedRoundRobin.increaseCurrent() % list.size();
    //     return list.get(index);
    // }

    @Override
    protected InstanceNode doSelect(List<InstanceNode> instanceNodes, String serviceKey) {
        long now = System.currentTimeMillis();
        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        InstanceNode selected = null;
        WeightedRoundRobin seletedWrr = null;
        // 获取当前服务下的结点及其计数值
        ConcurrentHashMap<String, WeightedRoundRobin> nodeToRoundRobin = serviceWeightMap.computeIfAbsent(serviceKey, k -> new ConcurrentHashMap<>());
        for (InstanceNode node : instanceNodes) {
            // 节点url作为uuid
            String uuid = node.getUrl();
            // 计算节点权值
            int weight = getWeight(node);
            // 拿到节点的轮循计数器
            WeightedRoundRobin weightedRoundRobin = nodeToRoundRobin.computeIfAbsent(uuid, k -> {
                WeightedRoundRobin wrr = new WeightedRoundRobin();
                wrr.setWeight(weight);
                return wrr;
            });
            // 更新节点权值
            if(weight != weightedRoundRobin.getWeight()) {
                weightedRoundRobin.setWeight(weight);
            }
            // 节点当前的调用编号
            long curr = weightedRoundRobin.increaseCurrent();
            // 记录轮序计数器上次使用的时间
            weightedRoundRobin.setLastUpdate(now);
            if(curr > maxCurrent) {
                maxCurrent = curr;
                selected = node;
                seletedWrr = weightedRoundRobin;
            }
            // 累加权重
            totalWeight += weight;
        }

        // 移除轮循计数器, 因为可能两次调用之间有一些新的节点上线或者下限导致节点列表长度不一致; 更新轮循计数器
        if(instanceNodes.size() != nodeToRoundRobin.size()) {
            nodeToRoundRobin.entrySet().removeIf(item -> now - item.getValue().getLastUpdate() > RECYCLE_PERIOD);
        }
        if(selected != null) {
            // 计数器向前平移
            seletedWrr.sel(totalWeight);
            return selected;
        }
        // should not happen here
        return instanceNodes.get(0);
    }
}
