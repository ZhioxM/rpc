package com.moon.rpc.service.impl;

import com.moon.rpc.api.service.HelloService;
import com.moon.rpc.server.annotation.RpcService;

/**
 * @author mzx
 * @date 2022/7/18 11:40
 */
@RpcService(version = "1.1")
public class HelloServiceImpl2 implements HelloService {
    @Override
    public String sayHello(String name) {
        // 测试异常
        // int i = 1 / 0;
        return "你好, " + name + "版本1.1";
    }
}
