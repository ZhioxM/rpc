package com.moon.rpc.transport.loadbalance;


import com.moon.rpc.transport.registry.InstanceNode;

import java.util.List;

/**
 * 负载均衡算法
 *
 * @author chenlei
 */
public interface LoadBalance {

    InstanceNode select(List<InstanceNode> list, String serviceKey);
}
