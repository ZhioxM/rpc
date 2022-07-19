package com.moon.rpc.client.exception;


/**
 * 客户端调用的超时异常
 *
 * @author mzx
 */
public class TimeoutException extends RpcException {
    private final int code = 0;

    public TimeoutException() {
    }

    public TimeoutException(String msg) {
        super(msg);
    }
}
