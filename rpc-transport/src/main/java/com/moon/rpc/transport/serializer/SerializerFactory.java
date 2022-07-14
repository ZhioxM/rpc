package com.moon.rpc.transport.serializer;

import com.moon.rpc.transport.serializer.impl.HessianSerializer;
import com.moon.rpc.transport.serializer.impl.JdkSerializer;
import com.moon.rpc.transport.serializer.impl.JsonSerializer;

/**
 * @Author: changjiu.wang
 * @Date: 2021/7/24 22:38
 */
public class SerializerFactory {

    public static RpcSerializer getRpcSerializer(byte type) {

        switch (type) {
            case 0:
                return new HessianSerializer();
            case 1:
                return new JsonSerializer();
            case 4:
                return new JdkSerializer();
            default:
                throw new IllegalArgumentException("serialization type is illegal");
        }
    }

}
