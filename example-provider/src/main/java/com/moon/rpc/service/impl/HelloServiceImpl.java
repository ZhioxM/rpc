package com.moon.rpc.service.impl;

import com.moon.rpc.api.service.HelloService;
import com.moon.rpc.server.annotation.RpcService;

/**
 * @author mzx
 * @date 2022/7/14 14:56
 */
@RpcService(version = "1.0")
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        //try {
        //    Thread.sleep(1000*5);
        //} catch (InterruptedException e) {
        //    throw new RuntimeException(e);
        //}
        return "你好" + name + "版本1.0";
    }
}
