package com.moon.rpc.client.exception;

/**
 * 自定义异常，参考Dubbo
 *
 * @author mzx
 * @date 2022/7/18 17:19
 */
public class RpcException extends RuntimeException {
    /**
     * 业务异常使用奇数, 非业务异常使用偶数
     */

    /** 超时异常 */
    public static final int TIMEOUT_EXCEPTION = 0;

    /**
     * 通信过程中发生的异常
     */
    public static final int TRANSPORT_EXCEPTION = 2;

    private int code;

    public RpcException() {
        super();
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }

    public boolean isTansaction() {
        return (code & 1) == 1;
    }
}
