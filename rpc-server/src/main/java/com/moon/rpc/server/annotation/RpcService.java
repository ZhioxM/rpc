package com.moon.rpc.server.annotation;


import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * 暴露服务的注解
 * 将使用了这个注解的服务注册到注册中心中
 *
 * @author mzx
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
// 复用使用Spring的注解，可以实现将Bean注入容器中
@Service
public @interface RpcService {

    /**
     * 暴露的服务名字
     *
     * @return
     */
    String name() default "";

    /**
     * 服务版本
     *
     * @return
     */
    String version() default "1.0";

    /**
     * 分组
     * @return
     */
    String groupName() default "";

    /**
     * 权重
     *
     * @return
     */
    int weight() default -1;
}