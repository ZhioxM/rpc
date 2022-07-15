package com.moon.rpc.controller;

import com.moon.rpc.api.service.HelloService;
import com.moon.rpc.client.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mzx
 * @date 2022/7/15 17:10
 */
@RestController
public class HelloController {
    @RpcReference(version = "1.0")
    private HelloService helloService;

    @GetMapping("/hello")
    private String hello() {
        return helloService.sayHello("张三");
    }
}
