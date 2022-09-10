package com.moon.rpc.client.factory;

import com.moon.rpc.transport.dto.RpcResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mzx
 * @desc 保存客户端异步响应结果的静态工厂
 */
public class LocalRpcResponseFactory {
    private static ConcurrentHashMap<Integer, CompletableFuture<RpcResponse>> req_res = new ConcurrentHashMap<>();

    public static void add(Integer reqId, CompletableFuture<RpcResponse> future) {
        // 已存在就覆盖
        req_res.put(reqId, future);
    }

    public static void remove(Integer reqId) {
        req_res.remove(reqId);
    }

    /**
     * 设置响应结果
     *
     * @param sequenceId
     * @param rpcResponse
     */
    public static void setResponse(Integer sequenceId, RpcResponse rpcResponse) {
        // 获取缓存中的 future（同时将其移除掉，因为他已经有响应结果了）
        CompletableFuture<RpcResponse> future = req_res.remove(sequenceId);
        if (future != null) {
            // 设置数据
            Exception e = rpcResponse.getException();
            if(e == null) {
                future.complete(rpcResponse);
            } else {
                future.completeExceptionally(e);
            }
        }
    }
}
