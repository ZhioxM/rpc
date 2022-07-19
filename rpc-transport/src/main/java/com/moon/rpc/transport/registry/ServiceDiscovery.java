package com.moon.rpc.transport.registry;


import com.alibaba.nacos.api.exception.NacosException;

import java.util.Set;

/**
 * 服务发现接口
 * 由客户端使用
 *
 * @author chenlei
 */
public interface ServiceDiscovery {

    /**
     * 根据服务名找到服务器节点
     */
    InstanceNode select(String serviceName, Set<InstanceNode> invoked) throws NacosException;

}
