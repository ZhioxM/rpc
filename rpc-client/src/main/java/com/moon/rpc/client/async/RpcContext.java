package com.moon.rpc.client.async;

import com.moon.rpc.client.factory.LocalRpcResponseFactory;
import com.moon.rpc.client.transport.ResponseFuture;
import com.moon.rpc.transport.dto.RpcResponse;

/**
 * @Author: Mzx
 * @Date: 2022/8/30 0:06
 */
public class RpcContext {
    private static final ThreadLocal<InvokeCompletableFuture<?>> RPC_RESPONSE_THREAD_LOCAL = new ThreadLocal<>();

    public static void setCompletableFuture(Integer sequenceId, ResponseFuture<RpcResponse> responseFuture, InvokeCompletableFuture<?> future) {
        // System.out.println("当前线程：" + Thread.currentThread().getName()); // 验证使用ThreadLocal存储InvokeCompletableFuture<?>的正确性
        LocalRpcResponseFactory.add(sequenceId, responseFuture);
        RPC_RESPONSE_THREAD_LOCAL.set(future);
    }

    public static <T> InvokeCompletableFuture<T> getCompletableFuture() {
        InvokeCompletableFuture<T> completableFuture = (InvokeCompletableFuture<T>) RPC_RESPONSE_THREAD_LOCAL.get();
        RPC_RESPONSE_THREAD_LOCAL.remove(); // get之后手动remove，防止内存泄露
        return completableFuture;
    }

    public static void removeCompletableFuture() {
        RPC_RESPONSE_THREAD_LOCAL.remove();
    }
}
