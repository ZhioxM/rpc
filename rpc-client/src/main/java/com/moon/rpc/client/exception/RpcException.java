package com.moon.rpc.client.exception;

/**
 * 自定义异常，参考Dubbo
 *
 * @author mzx
 * @date 2022/7/18 17:19
 */
public class RpcException extends RuntimeException {
    public static final int UNKNOWN_EXCEPTION = 0;
    public static final int NETWORK_EXCEPTION = 1;
    public static final int TIMEOUT_EXCEPTION = 2;
    public static final int BIZ_EXCEPTION = 3;
    public static final int FORBIDDEN_EXCEPTION = 4;
    public static final int SERIALIZATION_EXCEPTION = 5;
    public static final int NO_INVOKER_AVAILABLE_AFTER_FILTER = 6;
    public static final int LIMIT_EXCEEDED_EXCEPTION = 7;
    public static final int TIMEOUT_TERMINATE = 8;
    public static final int REGISTRY_EXCEPTION = 9;
    public static final int ROUTER_CACHE_NOT_BUILD = 10;
    public static final int METHOD_NOT_FOUND = 11;
    public static final int VALIDATION_EXCEPTION = 12;
    private static final long serialVersionUID = 7815426752583648734L;

    protected int code;

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

    public boolean isTimeOut() {
        return code == TIMEOUT_EXCEPTION;
    }
}
