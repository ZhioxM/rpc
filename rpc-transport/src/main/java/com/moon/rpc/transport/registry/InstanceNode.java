package com.moon.rpc.transport.registry;

import lombok.*;

import java.util.Map;

/**
 * 服务器节点类
 * 定义一个统一的，不使用Nacos的Instance类，这样就可以拓展注册中心
 *
 * @author mzx
 * @date 2022/7/18 19:21
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class InstanceNode {
    /**
     * 服务名称
     */
    // private String interfaceName;

    /**
     * 地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 权重
     */
    private Integer weight;

    /**
     * 元数据
     */
    private Map<String, String> metaData;

    public int getParameter(String key, int defaultValue) {
        if (metaData == null || !metaData.containsKey(key)) return defaultValue;
        return Integer.parseInt(metaData.get(key));
    }

    public long getParameter(String key, long defaultValue) {
        if (metaData == null || !metaData.containsKey(key)) return defaultValue;
        return Long.parseLong(metaData.get(key));
    }

    public String getUrl() {
        return host + ":" + port;
    }
}
