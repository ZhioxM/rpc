package com.moon.rpc.client.exception;

/**
 * RPC异常
 */
public class RemotingException extends RuntimeException{
    public RemotingException() {}

    public RemotingException(String msg) {
        super(msg);
    }
}
