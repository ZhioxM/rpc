package com.moon.rpc.transport.serializer.impl;

import com.moon.rpc.transport.serializer.RpcSerializer;

import java.io.*;

/**
 * @author mzx
 * @date 2022/7/14 17:37
 */
public class JdkSerializer implements RpcSerializer {

    @Override
    public <T> byte[] serialize(T object) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("序列化失败", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("反序列化失败", e);
        }
    }
}
