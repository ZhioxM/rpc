package com.moon.rpc.server.autoconfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author mzx
 * @date 2022/7/15 11:03
 */

/**
 * 此注解的作用是从配置未文件中读取属性，比@Value好用，与@EnablConfigurationProperties搭配使用
 */
@ConfigurationProperties(prefix = "rpc.server")
public class RpcServerProperties {
    /**
     * 服务器地址
     */
    private String host = "localhost";

    /**
     * 服务器端口
     */
    private Integer port = 8080;

    /**
     * 注册中心的地址
     */
    private String registryAddr = "localhost:8848";

    /**
     * 启动预热时间
     */
    private Integer warmUp;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getRegistryAddr() {
        return registryAddr;
    }

    public void setRegistryAddr(String registryAddr) {
        this.registryAddr = registryAddr;
    }

    public Integer getWarmUp() {
        return warmUp;
    }

    public void setWarmUp(Integer warm_up) {
        this.warmUp = warm_up;
    }
}
