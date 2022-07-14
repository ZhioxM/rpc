package com.moon.rpc.transport.serializer;

import java.io.IOException;

/**
 * @Author: changjiu.wang
 * @Date: 2021/7/4 13:36
 */
public interface RpcSerializer {
    <T> byte[] serialize(T obj) throws IOException;

    <T> T deserialize(byte[] data, Class<T> clz) throws IOException;
}