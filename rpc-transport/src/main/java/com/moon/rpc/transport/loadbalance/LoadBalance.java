package com.moon.rpc.transport.loadbalance;


import com.moon.rpc.transport.registry.Invoker;

import java.util.List;

/**
 * 负载均衡算法
 *
 * @author chenlei
 */
public interface LoadBalance {

    Invoker select(List<Invoker> list);

}
