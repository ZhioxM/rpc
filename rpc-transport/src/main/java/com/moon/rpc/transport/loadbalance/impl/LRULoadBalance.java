package com.moon.rpc.transport.loadbalance.impl;

import com.moon.rpc.transport.loadbalance.LoadBalance;
import com.moon.rpc.transport.registry.InstanceNode;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Author: Mzx
 * @Date: 2022/8/30 13:03
 */
public class LRULoadBalance implements LoadBalance {
    // 直接使用LinkedHashMap作LRU
    private final ConcurrentHashMap<String, LinkedHashMap<InstanceNode, InstanceNode>> serviceLRUMap = new ConcurrentHashMap<>();
    private long CACHE_VALID_TIME = 0;

    @Override
    public InstanceNode select(List<InstanceNode> list, String serviceKey) {
        // LRU缓存过期了，则清空
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            serviceLRUMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }
        // 获取当前服务的LRUCache
        // accessOrder= true即可实现LRU，此时链表尾部就是最近一次使用，链表头部就是最近最少使用的（在LRU中就是要被淘汰的，在这里就是这次要选择的节点）
        LinkedHashMap<InstanceNode, InstanceNode> lru = serviceLRUMap.computeIfAbsent(serviceKey, e -> new LinkedHashMap<InstanceNode, InstanceNode>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                if (super.size() > 1000) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        // 添加新的instanceNode到LRUCache中
        for (InstanceNode instanceNode : list) {
            if (!lru.containsKey(instanceNode)) {
                lru.put(instanceNode, instanceNode);
            }
        }

        // 删除LRUCache旧的InstanceNode
        HashSet<InstanceNode> nodeSet = new HashSet<>(list);
        List<InstanceNode> delNode = lru.keySet().stream().filter(new Predicate<InstanceNode>() {
            @Override
            public boolean test(InstanceNode instanceNode) {
                return !nodeSet.contains(instanceNode);
            }
        }).collect(Collectors.toList());
        for (InstanceNode node : delNode) {
            lru.remove(node);
        }

        // 链表头部的是最近最少使用的，所以这次选择它
        InstanceNode eldestKey = lru.keySet().iterator().next();
        return lru.get(eldestKey);
    }
}
