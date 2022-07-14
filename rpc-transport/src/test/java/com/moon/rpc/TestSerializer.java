package com.moon.rpc;

import com.moon.rpc.transport.dto.RpcRequest;
import com.moon.rpc.transport.serializer.impl.JdkSerializer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author mzx
 * @date 2022/7/14 17:59
 */
public class TestSerializer {
    public static void main(String[] args) {
        JdkSerializer jdkSerializer = new JdkSerializer();
        RpcRequest rpcRequest = new RpcRequest(
                1,
                "com.moon.rpc.api.service.HelloService",
                "sayHello",
                new Class[]{String.class},
                new String[]{"hello"},
                1000L,
                TimeUnit.MILLISECONDS
        );
        byte[] serialize = jdkSerializer.serialize(rpcRequest);
        try {
            RpcRequest request = jdkSerializer.deserialize(serialize, RpcRequest.class);
            System.out.println(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
