package com.moon.rpc.transport.constant;

import com.moon.rpc.transport.serializer.Serializer;

/**
 * @author mzx
 * @date 2022/7/13 14:46
 */
public class ProtocolConstant {
    public static final int MAGIC = 0x00;
    public static final byte VERSION = 0x1;

    public static final Serializer SERIALIZER = Serializer.HESSIAN;
}
