package com.moon.rpc.server.autoconfig;

import com.moon.rpc.server.RpcServer;
import com.moon.rpc.transport.registry.ServiceRegistry;
import com.moon.rpc.transport.registry.impl.NacosServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author mzx
 * @date 2022/7/15 11:02
 */
@Configuration
/** 此注解的作用相当于是将RpcServerProperties这个Bean注入容器中*/
@EnableConfigurationProperties(RpcServerProperties.class)
public class RpcServerAutoConfiguration {
    @Autowired
    private RpcServerProperties properties;

    /**
     * 创建服务注册中心客户端
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public ServiceRegistry serviceRegistry() {
        return new NacosServiceRegistry(properties.getRegistryAddr());
    }

    /**
     * 创建一个服务器对象
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(RpcServer.class)
    public RpcServer rpcServer() {
        return new RpcServer(properties.getHost(), properties.getPort());
    }

    /**
     * 创建一个后置处理器
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(RpcServerBeanPostProcessor.class)
    public RpcServerBeanPostProcessor rpcServerPostProcessor() {
        return new RpcServerBeanPostProcessor();
    }

}
