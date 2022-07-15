package com.moon.rpc.server.handler;


import com.moon.rpc.server.factory.LocalServiceFactory;
import com.moon.rpc.transport.dto.RpcRequest;
import com.moon.rpc.transport.dto.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author mzx
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {

    /**
     * 通过反射调用RPC请求的方法
     *
     * @param ctx
     * @param rpcRequest
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setSequenceId(rpcRequest.getSequenceId());
        try {
            // 通过反射调用方法，得到返回值
            Object result = handle(rpcRequest);
            response.setReturnValue(result);
        } catch (Exception e) {
            // 这个异常信息态长了，超出了长度字段解码器设置的1024的最大限制，就会在帧解码器那里报错了，所以我们不需要返回那么长的错误信息
            // response.setExceptionValue(e);
            response.setException(new Exception("远程调用出错:" + e.getCause().getMessage()));
        } finally {
            // 将响应对象返回给客户端，message对象在OutBound时会被编解码器编码成byteBuf
            ctx.writeAndFlush(response);
            // 释放ByteBuf, ? 啥意思？，这也不是ByteBuf啊
            ReferenceCountUtil.release(rpcRequest);
        }
    }

    private Object handle(RpcRequest rpcRequest) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 通过服务名称从本地工厂获取本地注解了@RpcSerice的实例对象
        Object service = LocalServiceFactory.getService(rpcRequest.getApiName(), rpcRequest.getVersion());
        // 获取调用的方法
        Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
        // 调用方法得到返回值
        return method.invoke(service, rpcRequest.getParameterValue());
    }
}
