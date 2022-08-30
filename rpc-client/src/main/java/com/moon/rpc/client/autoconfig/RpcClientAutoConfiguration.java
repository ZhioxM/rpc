package com.moon.rpc.client.autoconfig;


import com.moon.rpc.client.proxy.RpcClientProxy;
import com.moon.rpc.transport.constant.impl.RandomLoadBalance;
import com.moon.rpc.transport.constant.impl.RoundRobinLoadBalance;
import com.moon.rpc.transport.loadbalance.LoadBalance;
import com.moon.rpc.transport.registry.ServiceDiscovery;
import com.moon.rpc.transport.registry.impl.NacosServiceDiscovery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

/**
 * @author mzx
 * @date 2022/7/15 14:43
 */
@Configuration
@EnableConfigurationProperties(RpcClientProperties.class)
public class RpcClientAutoConfiguration {

    //@Bean
    //public RpcClientProperties rpcClientProperties(Environment environment) {
    //    BindResult<RpcClientProperties> result = Binder.get(environment).bind("rpc.client", RpcClientProperties.class);
    //    return result.get();
    //}


    /**
     * TODO 这么写的话，那么注解里面的配置的负载均衡器就失效了，所以不那个只注入一个负载均衡器
     * 默认是这个随机的代码均衡器
     *
     * @return
     */
    @Primary
    @Bean(name = "loadBalance")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rpc.client", name = "balance", havingValue = "randomBalance", matchIfMissing = true)
    @Order(0)
    public LoadBalance randomBalance() {
        System.out.println("random balance");
        return new RandomLoadBalance();
    }

    @Bean(name = "loadBalance")
    @ConditionalOnMissingBean
    @Order(0)
    @ConditionalOnProperty(prefix = "rpc.client", name = "balance", havingValue = "round")
    public LoadBalance roundRobinLoadBalance() {
        System.out.println("round robin");
        return new RoundRobinLoadBalance();
    }

    /**
     * 配置服务发现类
     *
     * @param loadBalance
     * @return
     */
    @Bean
    //@ConditionalOnBean({RpcClientProperties.class, LoadBalance.class})
    @Order(2)
    public ServiceDiscovery serviceDiscovery(@Autowired RpcClientProperties rpcClientProperties, @Autowired LoadBalance loadBalance) {
        System.out.println("discovery");
        return new NacosServiceDiscovery(rpcClientProperties.getRegistryAddr(), loadBalance);
    }

    @Bean
    @DependsOn("serviceDiscovery")
    public RpcClientProxy rpcClientProxy(@Autowired ServiceDiscovery serviceDiscovery) {
        System.out.println("初始化rpcClientProxy");
        return new RpcClientProxy(serviceDiscovery);
    }

    @Bean
    //@ConditionalOnBean(name = "rpcClientProxy")
    @DependsOn("rpcClientProxy")
    @Order(4)
    public RpcClientBeanPostProcessor rpcClientProcessor(@Autowired RpcClientProxy rpcClientProxy) {
        System.out.println("初始化process");
        return new RpcClientBeanPostProcessor(rpcClientProxy);
    }
}
