package com.moon.rpc.client.handler;

import com.moon.rpc.client.factory.LocalRpcResponseFactory;
import com.moon.rpc.transport.dto.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author mzx
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        // 当前线程是某个EventLoop
        log.debug("{}", rpcResponse);
        LocalRpcResponseFactory.setResponse(rpcResponse.getSequenceId(), rpcResponse);
    }
}
