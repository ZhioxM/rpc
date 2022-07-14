package com.moon.rpc.transport.serializer;

import lombok.Getter;

/**
 * @Author: changjiu.wang
 * @Date: 2021/7/25 5:28
 */
public enum Serializer {

    HESSIAN((byte) 0),
    JSON((byte) 1),
    KROV((byte) 2),
    PROTUBUF((byte) 3),
    JAVA((byte) 4);

    @Getter
    private byte type;

    Serializer(byte type) {
        this.type = type;
    }

}
