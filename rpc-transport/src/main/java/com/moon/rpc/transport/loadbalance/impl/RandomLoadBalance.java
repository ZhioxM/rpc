package com.moon.rpc.transport.loadbalance.impl;

import com.moon.rpc.transport.registry.InstanceNode;

import java.util.List;
import java.util.Random;

/**
 * 随机权重算法
 * 普通的线性扫描实现
 * 补充资料：https://blog.csdn.net/haha1fan/article/details/106609910
 *
 * @author chenlei
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    private final Random random = new Random();

    /**
     * @param instanceNodes
     * @param serviceKey
     * @return
     */
    @Override
    protected InstanceNode doSelect(List<InstanceNode> instanceNodes, String serviceKey) {
        int length = instanceNodes.size();
        int totalWeight = 0;
        // 检测是不是所有的服务提供者的权重都完全相同
        boolean sameWeight = true;
        for (int i = 0; i < length; i++) {
            int weight = getWeight(instanceNodes.get(i));
            totalWeight += weight;
            if (sameWeight && i > 0 && weight != getWeight(instanceNodes.get(i - 1))) {
                sameWeight = false;
            }
        }
        if (!sameWeight && totalWeight > 0) {
            // 获取随机权重：随机获取一个在[0, totalWeight)范围内的数字
            int offset = random.nextInt(totalWeight);
            // 普通线性扫描：当前offset小于当前节点的权重，则选择当前节点，否则减去当前节点的权重
            for (int i = 0; i < length; i++) {
                offset -= getWeight(instanceNodes.get(i));
                if (offset < 0) {
                    return instanceNodes.get(i);
                }
            }
        }
        // 权值之和为0或者所有节点的权值相同，则退化为随机算法
        return instanceNodes.get(random.nextInt(length));
    }

    /**
     * 随机权重的另一种实现方法，有序的线性扫描
     * 每次都重新计算节点的权重，效率是不是太低了，Dubbo为什么不直接计算一次，将结果存在节点的权重属性中呢
     *
     * @param instanceNodes
     * @param serviceKey
     * @return
     */
    protected InstanceNode doSelectOrder(List<InstanceNode> instanceNodes, String serviceKey) {
        int length = instanceNodes.size();
        int totalWeight = 0;
        // 检测是不是所有的服务提供者的权重都完全相同
        boolean sameWeight = true;
        for (int i = 0; i < length; i++) {
            int weight = getWeight(instanceNodes.get(i));
            totalWeight += weight;
            if (sameWeight && i > 0 && weight != getWeight(instanceNodes.get(i - 1))) {
                sameWeight = false;
            }
        }
        // 根据权重降序排序
        instanceNodes.sort((a, b) -> getWeight(b) - getWeight(a));
        // 有序的线性扫描
        // 比如有权重降序排序为[5, 3, 2]，分别对应节点A, B, C
        // 那么节点A的区间为[0, 5), 节点B的区间为[5, 8), 节点C区间为[8, 10), 判断随机权重落在那个区间就可以进行选择
        if (!sameWeight && totalWeight > 0) {
            // 获取随机权重：随机获取一个在[0, totalWeight)范围内的数字
            int offset = random.nextInt(totalWeight);
            // 普通线性扫描：当前offset小于当前节点的权重，则选择当前节点，否则减去当前节点的权重
            for (int i = 0; i < length; i++) {
                offset -= getWeight(instanceNodes.get(i));
                if (offset < 0) {
                    return instanceNodes.get(i);
                }
            }
        }
        // 权值之和为0或者所有节点的权值相同，则退化为随机算法
        return instanceNodes.get(random.nextInt(length));
    }
}
