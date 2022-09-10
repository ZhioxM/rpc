package com.moon.rpc.client.async;

import com.moon.rpc.client.transport.ResponseFuture;
import com.moon.rpc.transport.dto.RpcResponse;
import com.moon.rpc.transport.exception.RpcException;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    public T get() {
        try {
            // 默认超时时间是三秒，或者设置为-1：永不超时
            return get(3000, TimeUnit.MILLISECONDS);
        } catch (RpcException e) {
            throw e;
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        RpcResponse rpcResponse = responseFuture.get(timeout, unit);
        if (rpcResponse.getException() != null) {
            throw new RpcException(RpcException.BIZ_EXCEPTION, rpcResponse.getException().getMessage());
        }
        return (T) rpcResponse.getReturnValue();
    }
}
