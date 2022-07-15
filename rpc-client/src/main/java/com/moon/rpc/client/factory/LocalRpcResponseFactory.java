package com.moon.rpc.client.factory;

import com.moon.rpc.client.transport.ResponseFuture;
import com.moon.rpc.transport.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mzx
 * @desc 保存客户端异步响应结果的静态工厂
 */
public class LocalRpcResponseFactory {
    private static Map<Integer, ResponseFuture<RpcResponse>> req_res = new ConcurrentHashMap<>();

    public static void add(Integer reqId, ResponseFuture<RpcResponse> future){
        req_res.put(reqId, future);
    }

    /**
     * 设置响应结果
     * @param reqId
     * @param rpcResponse
     */
    public static void setResponse(Integer reqId, RpcResponse rpcResponse){
        // 获取缓存中的 future（同时将其移除掉，因为他已经有响应结果了）
        ResponseFuture<RpcResponse> future = req_res.remove(reqId);
        if(future != null) {
            // 设置数据
            future.setResponse(rpcResponse);
        }
    }
}
