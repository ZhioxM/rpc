package com.moon.rpc.transport.constant.impl;

import com.moon.rpc.transport.loadbalance.LoadBalance;
import com.moon.rpc.transport.registry.InstanceNode;

import java.util.List;
import java.util.Random;

/**
 * 负载均衡随机算法
 *
 * @author chenlei
 */
public class RandomLoadBalance implements LoadBalance {
    private final Random random = new Random();

    /**
     * 随机获取实例
     *
     * @param list
     * @return
     */
    @Override
    public InstanceNode select(List<InstanceNode> list, String serviceKey) {
        return list.get(random.nextInt(list.size()));
    }
}
