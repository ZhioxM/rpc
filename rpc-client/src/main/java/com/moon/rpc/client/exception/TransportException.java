package com.moon.rpc.client.exception;

/**
 * @author mzx
 * @date 2022/7/19 11:51
 */
public class TransportException extends RpcException {
    private final int code = 2;

    public TransportException() {
    }

    public TransportException(String msg) {
        super(msg);
    }
}
