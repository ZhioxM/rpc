package com.moon.rpc.transport.registry;

import lombok.*;

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
public class Invoker {
    /**
     * 服务名称
     */
    private String interfaceName;

    /**
     * 地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;
}
