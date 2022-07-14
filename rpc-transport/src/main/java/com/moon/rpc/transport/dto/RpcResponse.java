package com.moon.rpc.transport.dto;

import com.moon.rpc.transport.constant.MessageType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author mzx
 * @date 2022-07-13 14:30:33
 */
@Getter
@Setter
public class RpcResponse extends Message{
    /**
     * 正常调用的返回值
     */
    private Object returnValue;

    /**
     * 异常调用的失败信息
     */
    private Exception exception;

    public RpcResponse() {
        this.messageType = MessageType.RESPONSE.getType();
    }

    @Override
    public String toString() {
        return "RpcResponse{" +
                "returnValue=" + returnValue +
                ", exception=" + exception +
                ", sequenceId=" + sequenceId +
                ", messageType=" + messageType +
                '}';
    }
}
