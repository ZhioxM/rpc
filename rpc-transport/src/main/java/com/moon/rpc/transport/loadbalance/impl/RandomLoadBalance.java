package com.moon.rpc.transport.loadbalance.impl;

import com.moon.rpc.transport.loadbalance.LoadBalance;
import com.moon.rpc.transport.registry.Invoker;

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
    public Invoker select(List<Invoker> list) {
        return list.get(random.nextInt(list.size()));
    }
}
