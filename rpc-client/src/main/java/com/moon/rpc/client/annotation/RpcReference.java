package com.moon.rpc.client.annotation;

import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.*;

/**
 * @author mzx
 * @date 2022/7/15 14:31
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
// 复用Autowired的功能
@Autowired
public @interface RpcReference {
    /**
     * 接口 暂时不用
     *
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 接口的名字 暂时不用
     *
     * @return
     */
    String interfaceName() default "";

    /**
     * 版本号
     *
     * @return
     */
    String version();

    /**
     * 超时时间，单位ms
     *
     * @return
     */
    long timeout() default 10000;

    /**
     * 重试次数
     *
     * @return
     */
    int retries() default 2;

    /**
     * 负载均衡器 暂时不用
     *
     * @return
     */
    String loadbalance() default "";

    /**
     * 是否采用异步调用的方式
     *
     * @return
     */
    boolean async() default false;

    /**
     * 异步调用是否需要返回值，即是否是oneWay
     * @return
     */
    boolean oneWay() default false;
}
