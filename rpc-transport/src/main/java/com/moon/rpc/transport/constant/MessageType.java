package com.moon.rpc.transport.constant;

import lombok.Getter;

/**
 * @author mzx
 * @date 2022/7/13 14:49
 */
public enum MessageType {
    /**
     * 请求消息
     */
    REQUEST((byte) 1),

    /**
     * 响应消息
     */
    RESPONSE((byte) 2),

    /**
     * 心跳包
     */
    HEARTBEAT((byte) 3);

    @Getter
    private byte type;

    MessageType(byte type) {
        this.type = type;
    }

    public static MessageType get(byte type) {
        for (MessageType msgType : MessageType.values()) {
            if (msgType.getType() == type) {
                return msgType;
            }
        }
        return null;
    }
}
