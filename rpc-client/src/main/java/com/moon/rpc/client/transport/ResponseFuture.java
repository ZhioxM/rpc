package com.moon.rpc.client.transport;

import java.util.concurrent.TimeUnit;

/**
 * 自己写一个future, 不使用CompleteFuture了
 */
public interface ResponseFuture<T> {
    /**
     * 获取响应结果
     * @return
     */
    T get();


    /**
     * 等待一段时间获取结果
     * @param timeout
     * @param timeUnit
     * @return
     */
    T get(long timeout, TimeUnit timeUnit);

    /**
     * 设置响应结果
     * @param response
     */
    void setResponse(T response);

    /**
     * 判断是否拿到了响应结果了
     * @return
     */
    boolean isDone();
}
