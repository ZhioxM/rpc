package com.moon.rpc.controller;

import com.moon.rpc.client.annotation.RpcReference;
import com.moon.rpc.client.async.RpcContext;
import com.moon.rpc.client.generic.RpcGenericService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author: M
 * @Date: 2022/9/6 14:40
 */
@RestController
public class GenericHello {
    @RpcReference(version = "1.0", async = true)
    private RpcGenericService genericService;

    @GetMapping("/generic")
    public String generic(String name) {
        genericService.invoke("com.moon.rpc.api.service.HelloService",
                              "sayHello", new Class[]{String.class}, new Object[]{name});
        CompletableFuture<String> future = RpcContext.getContext().getFuture();
        try {
            return future.get(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
