package com.moon.rpc.server;

import com.moon.rpc.server.handler.HeartBeatServerHandler;
import com.moon.rpc.server.handler.RpcRequestHandler;
import com.moon.rpc.transport.codec.MessageCodecSharable;
import com.moon.rpc.transport.codec.ProcotolFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Rpc服务器
 *
 * @author mzx
 */
@Slf4j
public class RpcServer {
    // TODO 如果要打大包成springboot starter的话，需要SPI的相关知识，请看roc2的resource/META-INF/spring.factory

    // 对于本机，只能是localhost
    private final String host;
    private final int port;

    private NioEventLoopGroup boss = new NioEventLoopGroup();
    private NioEventLoopGroup work = new NioEventLoopGroup();
    private ServerBootstrap serverBootstrap = new ServerBootstrap();

    public RpcServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 启动服务器
     */
    public void start() {
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        RpcRequestHandler RPC_REQUEST_HANDLER = new RpcRequestHandler();
        HeartBeatServerHandler HEART_BEAT_SERVER_HANDLER = new HeartBeatServerHandler();
        try {
            serverBootstrap.group(boss, work)
                           .channel(NioServerSocketChannel.class)
                           .option(ChannelOption.SO_BACKLOG, 256)
                           .option(ChannelOption.SO_KEEPALIVE, true)
                           .option(ChannelOption.TCP_NODELAY, true)
                           .childHandler(new ChannelInitializer<NioSocketChannel>() {
                               @Override
                               protected void initChannel(NioSocketChannel ch) throws Exception {
                                   ch.pipeline()
                                     // 服务端的readerIDleTime和客户端的writerIdleTime一般是两倍的关系
                                     .addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS)) // 这个时间是什么时间？
                                     .addLast(new ProcotolFrameDecoder())
                                     .addLast(MESSAGE_CODEC)
                                     .addLast(LOGGING_HANDLER) // 这个和上面一个的顺序对功能没有影响，只是控制台打印效果不同而已
                                     .addLast(HEART_BEAT_SERVER_HANDLER) // 长时间没有接收到客户端的数据，说明网络有可能真的断开了，所以释放掉这条通道。而且不会出现连接假死的问题，因为客户端一直在发送数心跳包，如果连心跳包都没收到，说明连接大概率断开了
                                     .addLast(RPC_REQUEST_HANDLER);
                               }
                           });
            ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(this.host, this.port)).sync();
            log.info("服务器{}:{}成功启动...", this.host, this.port);
            Channel channel = channelFuture.channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("启动服务器时有错误发生: ", e);
        } finally {
            Future<?> bossCloseFuture = boss.shutdownGracefully();
            Future<?> workerCloseFuture = work.shutdownGracefully();
            try {
                bossCloseFuture.sync();
                workerCloseFuture.sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("服务端优雅关闭");
        }
    }

}
