package com.moon.rpc.controller;

import com.moon.rpc.api.service.HelloService;
import com.moon.rpc.client.async.RpcContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @Author: Mzx
 * @Date: 2022/8/30 10:14
 */
// @RestController
@Slf4j
public class HelloAsyncController {
    // @RpcReference(version = "1.1", async = true)
    private HelloService helloService;

    @GetMapping("/hello/async")
    private String hello() throws ExecutionException, InterruptedException {
        // System.out.println("当前线程：" + Thread.currentThread().getName()); // 验证使用ThreadLocal存储InvokeCompletableFuture<?>的正确性
        // 不阻塞
        System.out.println("不阻塞: 返回结果是: " + helloService.sayHello("张三"));
        log.info("invoking....");
        CompletableFuture<String> future = RpcContext.getContext().getFuture();
        // 模拟在做其他事
        Thread.sleep(3000);
        // 拿结果
        String s = null;
        try {
            s = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        log.info("invoke completed, return value: " + s);
        return s;
    }
}
