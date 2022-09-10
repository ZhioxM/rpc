package com.moon.rpc.client.transport;

import com.moon.rpc.transport.exception.RpcException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author mzx
 */
public class DefaultResponseFuture<T> implements ResponseFuture<T> {
    private T rpcResponse;

    /**
     * 因为请求和响应是一一对应的，所以这里是1
     * 使用 CountDownLatch 等待线程
     */
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Override
    public T get() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return rpcResponse;
    }

    @Override
    public T get(long timeout, TimeUnit timeUnit) {
        try {
            if (countDownLatch.await(timeout, timeUnit)) {
                return rpcResponse;
            } else {
                throw new RpcException(RpcException.TIMEOUT_EXCEPTION, "Call fails, timeout");
            }
        } catch (InterruptedException e) {
            throw new RpcException(RpcException.UNKNOWN_EXCEPTION, "Call fails, interrupted");
        }
    }

    @Override
    public void setResponse(T response) {
        this.rpcResponse = response;
        countDownLatch.countDown();
    }

    @Override
    public boolean isDone() {
        return rpcResponse != null;
    }
}
