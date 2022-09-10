package com.moon.rpc.client.async;

import java.util.concurrent.CompletableFuture;

/**
 * @Author: Mzx
 * @Date: 2022/8/30 0:06
 */
public class RpcContext {
    private static final ThreadLocal<RpcContext> RPC_RESPONSE_THREAD_LOCAL = new ThreadLocal<>();

    private CompletableFuture<?> future;

    public static RpcContext getContext() {
        RpcContext rpcContext = RPC_RESPONSE_THREAD_LOCAL.get();
        if(rpcContext == null) {
            RPC_RESPONSE_THREAD_LOCAL.set(new RpcContext());
        }
        return RPC_RESPONSE_THREAD_LOCAL.get();
    }

    public void setFuture(CompletableFuture<?> future) {
        // System.out.println("当前线程：" + Thread.currentThread().getName()); // 验证使用ThreadLocal存储InvokeCompletableFuture<?>的正确性
        this.future = future;
    }

    public <T> CompletableFuture<T> getFuture() {
        RPC_RESPONSE_THREAD_LOCAL.remove(); // get之后手动remove，防止内存泄露
        return (CompletableFuture<T>) future;
    }
}
