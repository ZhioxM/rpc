package com.moon.rpc.client.async;

import com.moon.rpc.client.exception.TransactionException;
import com.moon.rpc.client.transport.ResponseFuture;
import com.moon.rpc.transport.dto.RpcResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author: Mzx
 * @Date: 2022/8/30 9:44
 */
public class InvokeCompletableFuture<T> implements Future<T> {
    private final ResponseFuture<RpcResponse> responseFuture;

    public InvokeCompletableFuture(ResponseFuture<RpcResponse> responseFuture) {
        this.responseFuture = responseFuture;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        try {
            // 默认超时时间是三秒，或者设置为-1：永不超时
            return get(3000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // future get
        RpcResponse rpcResponse =  responseFuture.get(timeout, unit);
        if (rpcResponse.getException() != null) {
            throw new TransactionException(rpcResponse.getException().getMessage());
        }
        return (T) rpcResponse.getReturnValue();
    }
}
