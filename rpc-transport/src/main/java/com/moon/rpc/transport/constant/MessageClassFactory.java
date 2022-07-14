package com.moon.rpc.transport.constant;

import com.moon.rpc.transport.dto.HeartBeat;
import com.moon.rpc.transport.dto.Message;
import com.moon.rpc.transport.dto.RpcRequest;
import com.moon.rpc.transport.dto.RpcResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mzx
 * @date 2022/7/13 15:26
 * 存储各种消息类型的CLass对象，用于反序列
 */
public class MessageClassFactory {
    private static Map<Byte, Class<? extends Message>> factory = new HashMap<>();

    static {
        factory.put(MessageType.REQUEST.getType(), RpcRequest.class);
        factory.put(MessageType.RESPONSE.getType(), RpcResponse.class);
        factory.put(MessageType.HEARTBEAT.getType(), HeartBeat.class);
    }

    public static Class<? extends Message> get(byte messageType) {
        return factory.get(messageType);
    }
}
