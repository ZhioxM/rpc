package com.moon.rpc.transport.loadbalance;


import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 负载均衡算法
 *
 * @author chenlei
 */
public interface LoadBalance {

    Instance getInstance(List<Instance> list);

}
