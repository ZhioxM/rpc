package com.moon.rpc.client.autoconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Classname RpcClientProperties
 * @Description
 * @Date 2021/7/7 14:40
 * @Created by wangchangjiu
 */
@ConfigurationProperties(prefix = "rpc.client")
public class RpcClientProperties {

    /**
     * 负载均衡器
     */
    private String balance;

    /**
     * 序列化
     */
    private String serializer;

    /**
     * 注册中心地址
     */
    private String registryAddr = "127.0.0.1:8848";

    /**
     * 服务调用超时
     */
    private Long timeout;

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getSerializer() {
        return serializer;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public String getRegistryAddr() {
        return registryAddr;
    }

    public void setRegistryAddr(String registryAddr) {
        this.registryAddr = registryAddr;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}
