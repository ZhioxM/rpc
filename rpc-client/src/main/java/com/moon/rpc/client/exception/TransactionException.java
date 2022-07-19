package com.moon.rpc.client.exception;

/**
 * @author mzx
 * @date 2022/7/19 13:06
 */
public class TransactionException extends RpcException {
    private final int code = 1;

    public TransactionException() {
    }

    ;

    public TransactionException(String msg) {
        super(msg);
    }

}
