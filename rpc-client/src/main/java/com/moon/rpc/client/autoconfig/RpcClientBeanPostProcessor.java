package com.moon.rpc.client.autoconfig;

import com.moon.rpc.client.annotation.RpcReference;
import com.moon.rpc.client.proxy.RpcClientProxy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author mzx
 * @date 2022/7/15 15:21
 * BeanFactoryPostProcess 与 BeanPostProcessor接口的区别
 */
public class RpcClientBeanPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    /**
     * @Autowired没用
     */
    private RpcClientProxy rpcClientProxy;

    public RpcClientBeanPostProcessor(RpcClientProxy rpcClientProxy) {
        this.rpcClientProxy = rpcClientProxy;
    }

    ApplicationContext applicationContext;

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 涉及到源码和反射的知识了，这里不太懂
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.getClass().getClassLoader());
                ReflectionUtils.doWithFields(clazz, field -> {
                    RpcReference rpcReference = AnnotationUtils.getAnnotation(field, RpcReference.class);
                    if (rpcReference != null) {
                        Object bean = applicationContext.getBean(clazz);
                        field.setAccessible(true);
                        // 修改为代理对象（通过这种方式，在客户端获取接口实例的时候，就变成代理对象了）
                        ReflectionUtils.setField(field, bean, rpcClientProxy.getProxy(field.getType(), rpcReference));
                    }
                });
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
