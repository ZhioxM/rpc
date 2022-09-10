package com.moon.rpc.transport.loadbalance.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.moon.rpc.transport.loadbalance.LoadBalance;
import com.moon.rpc.transport.registry.InstanceNode;

import java.util.List;

import static com.moon.rpc.transport.constant.Constants.*;

/**
 * @Author: Mzx
 * @Date: 2022/9/7 8:05
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public InstanceNode select(List<InstanceNode> instanceNodes, String serviceKey) {
        if (CollectionUtils.isEmpty(instanceNodes)) {
            return null;
        }
        if (instanceNodes.size() == 1) {
            return instanceNodes.get(0);
        }
        return doSelect(instanceNodes, serviceKey);
    }

    protected abstract InstanceNode doSelect(List<InstanceNode> instanceNodes, String serviceKey);
    /**
     * 下面是权重的计算过程，该过程主要用于保证当服务运行时长小于服务预热时间时，对服务进行降权，避免让服务在启动之初就处于高负载状态。
     * 服务预热是一个优化手段，与此类似的还有 JVM 预热。主要目的是让服务启动后“低功率”运行一段时间，使其效率慢慢提升至最佳状态。
     * @param instanceNode
     * @return
     */
    protected int getWeight(InstanceNode instanceNode) {
        int weight = instanceNode.getWeight() == 0 ? DEFAULT_WEIGHT : instanceNode.getWeight();
        if(weight > 0) {
            // 获取服务启动者的启动时间戳
            long timestamp = instanceNode.getParameter(REMOTE_TIMESTAMP_KEY, 0L);
            if(timestamp > 0L) {
                // 计算服务提供者的运行时长
                int uptime = (int) (System.currentTimeMillis() - timestamp);
                // 获取服务启动预热时间, 默认为十分钟
                int warmup = instanceNode.getParameter(WARM_UP_KEY, DEFAULT_WARM_UP);
                // 如果服务运行时间小于预热时间，则需要重新计算权重，即降权的效果，实现优雅启动，避免大量的流量用于刚启动的服务器
                if(uptime > 0 && uptime < warmup) {
                    // 重新计算服务权重
                    weight = calculateWarmupWeight(uptime, warmup, weight);
                }
            }
        }
        return weight;
    }

    /**
     * @param uptime
     * @param warmup
     * @param weight
     * @return
     */
    static int calculateWarmupWeight(int uptime, int warmup, int weight) {
        // 计算权重，下面的代码逻辑形式上近似于 （uptime / warmup） * weight
        // 随着服务运行时间 uptime, 权重计算值会慢慢接近配置值weight
        int ww = (int) ( uptime / ((float) warmup / weight));
        return ww < 1 ? 1 : (Math.min(ww, weight));
    }
}
