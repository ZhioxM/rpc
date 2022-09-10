package com.moon.rpc.client.handler;

import com.moon.rpc.transport.constant.MessageType;
import com.moon.rpc.transport.dto.HeartBeat;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端的心跳handler
 * ChannelDuplexHandler是一个双向的Handler
 *
 * @author chenlei
 */
@Slf4j
@ChannelHandler.Sharable
public class HeartBeatClientHandler extends ChannelDuplexHandler {

    /**
     * 有用户事件触发时会调用这个函数
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 判断事件的类型
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            //长时间没有写入数据  发送心跳包
            if (event.state() == IdleState.WRITER_IDLE) {
                // 显示ip
                HeartBeat heartBeat = new HeartBeat();
                heartBeat.setSequenceId(0);
                heartBeat.setMessageType(MessageType.HEARTBEAT.getType());
                // 发送心跳包，如果发送失败，则异步关闭这条通道
                ctx.writeAndFlush(heartBeat).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }

        }
    }

    // @Override
    // public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    //     log.debug("远程调用出错");
    //     cause.printStackTrace();
    //     ctx.close();
    //     super.exceptionCaught(ctx, cause);
    // }


    // @Override
    // public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    //     log.debug("channelUnregistered");
    //     ctx.close();
    //     super.channelUnregistered(ctx);
    // }


}
