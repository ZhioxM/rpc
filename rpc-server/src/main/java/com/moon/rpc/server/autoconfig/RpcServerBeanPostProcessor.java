package com.moon.rpc.server.autoconfig;

import com.moon.rpc.server.RpcServer;
import com.moon.rpc.server.annotation.RpcService;
import com.moon.rpc.server.factory.LocalServiceFactory;
import com.moon.rpc.transport.registry.ServiceRegistry;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;

import static com.moon.rpc.transport.constant.Constants.DEFAULT_GROUP_NAME;
import static com.moon.rpc.transport.constant.Constants.DEFAULT_WEIGHT;

/**
 * @author mzx
 * @date 2022/7/15 13:29
 * 后置处理器，负责扫描注解，进行服务注册，启动RpcServer
 * 实现postProcessAfterInitialization这个方法，可以自定义一些逻辑，每个Bean在初始化之后都可以执行这个方法
 */
@Slf4j
public class RpcServerBeanPostProcessor implements BeanPostProcessor, CommandLineRunner {

    @Autowired
    private RpcServer rpcServer;

    @Autowired
    private RpcServerProperties rpcServerProperties;

    @Autowired
    private ServiceRegistry serviceRegistry;

    /**
     * 在每个Bean被实例化后执行
     *
     * @param bean     就是被实例化的对象
     * @param beanName 就是bean的名字
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 判断这个Bean是不是被RpcService注解修饰
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            // 获取这个Bean对应的类上的RpcService注解
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            try {
                // 获取接口名字(如果没有指定接口的名字，则使用自身实现的接口的名字，使用getName)
                String serviceName = StringUtil.isNullOrEmpty(rpcService.name()) ? bean.getClass().getInterfaces()[0].getName() : rpcService.name();
                // 获取版本
                String version = rpcService.version();
                // 获取权值
                int weight = rpcService.weight();
                // 获取分组
                String groupName = StringUtil.isNullOrEmpty(rpcService.groupName()) ? DEFAULT_GROUP_NAME : rpcService.groupName();
                // 保存服务接口
                LocalServiceFactory.addService(serviceName, version, bean);
                // 服务注册
                serviceRegistry.register(serviceName, rpcServerProperties.getHost(), rpcServerProperties.getPort(), groupName, weight == -1 ? DEFAULT_WEIGHT: weight);
            } catch (Exception ex) {
                log.error("服务注册出错:{}", ex);
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public void run(String... args) throws Exception {
        // TODO 有必要开新的线程？
        new Thread(() -> {
            rpcServer.start();
        }).run();
    }
}
