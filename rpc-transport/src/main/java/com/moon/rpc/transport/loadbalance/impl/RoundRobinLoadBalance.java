package com.moon.rpc.transport.loadbalance.impl;

import com.moon.rpc.transport.loadbalance.LoadBalance;
import com.moon.rpc.transport.registry.Invoker;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡算法
 *
 * @author chenlei
 */
public class RoundRobinLoadBalance implements LoadBalance {

    /**
     * 使用院子计数器做轮循
     */
    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * 防止Integer越界 超过Integer最大值
     *
     * @return
     */
    private final int getAndIncrement() {
        int current;
        int next;
        do {
            current = this.atomicInteger.get();
            next = current >= 2147483647 ? 0 : current + 1;
        } while (!this.atomicInteger.compareAndSet(current, next));
        return next;
    }


    /**
     * 轮询获取实例
     *
     * @param list
     * @return
     */
    @Override
    public Invoker select(List<Invoker> list) {
        int index = getAndIncrement() % list.size();
        return list.get(index);
    }
}
