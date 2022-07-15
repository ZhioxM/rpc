package com.moon.rpc.server.factory;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端本地的服务工厂
 *
 * @author chenlei
 */
@Slf4j
public class LocalServiceFactory {

    /**
     * 保存所有有注解@RpcService的类的实例化对象的集合
     * Key 是接口的名字 + 版本号，Value是接口的实现类对象
     */
    public static final Map<String, Object> SERVICE_FACTORY = new ConcurrentHashMap<>();

    /**
     * 添加已注解的类对象进入工厂
     */
    public static <T> void addService(String serviceName, String version, T service) {
        String key = serviceName + "-" + version;
        if (SERVICE_FACTORY.containsKey(key)) {
            return;
        }
        SERVICE_FACTORY.put(key, service);
        log.debug("{}添加进LocalServiceFactory, 版本号{}", serviceName, version);
    }

    /**
     * 远程调用接口从该方法获取
     */
    public static Object getService(String serviceName, String version) {
        Object service = SERVICE_FACTORY.get(serviceName + "-" + version);
        if (service == null) {
            throw new RuntimeException("服务端未发现该服务");
        }
        return service;
    }
}
