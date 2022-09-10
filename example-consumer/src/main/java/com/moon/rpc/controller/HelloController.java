package com.moon.rpc.controller;

import com.moon.rpc.api.service.HelloService;
import com.moon.rpc.client.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author mzx
 * @date 2022/7/15 17:10
 */
// @RestController
public class HelloController {
    // TODO：注意，HelloService这个Bean只会创建一个（BeanFactoryPostProcessor那里），所以HelloController里面的HelloService要注释掉。这不好，Spring怎么解决？
    @RpcReference(version = "1.1", timeout = 1000)
    private HelloService helloService;

    @GetMapping("/hello")
    private String hello() {
        return helloService.sayHello("张三");
    }
}
