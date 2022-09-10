package com.moon.rpc.client.factory;


import com.moon.rpc.client.handler.HeartBeatClientHandler;
import com.moon.rpc.client.handler.RpcResponseHandler;
import com.moon.rpc.transport.registry.InstanceNode;
import com.moon.rpc.transport.registry.codec.MessageCodecSharable;
import com.moon.rpc.transport.registry.codec.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 提供客户端连接
 * 网络连接的部分在此实现
 *
 * @author ziyang
 */
@Slf4j
public class RemoteChannelFactory {
    public static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private static final Bootstrap bootstrap = initializeBootstrap();

    /**
     * 客户端与服务端的所有连接通道
     * key:host+port -> value:channel
     */
    private static final Map<String, Channel> channels = new ConcurrentHashMap<>();

    /**
     * 根据地址获取Channel，如果这个channel不存在或者已经断开，则重新连接
     *
     * @param instanceNode
     * @return
     * @throws InterruptedException
     */
    public static Channel get(InstanceNode instanceNode) {
        String key = instanceNode.getHost() + ":" + instanceNode.getPort();
        if (channels.containsKey(key)) {
            // 连接存在
            Channel channel = channels.get(key);
            // 连接有效
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                // 连接失效了，则从channel集合中删除
                channels.remove(key);
            }
        }


        // 连接不存在或者失效，则重新建立连接
        Channel channel = null;
        try {
            log.debug("重新连接");
            channel = connect(bootstrap, instanceNode);
        } catch (ExecutionException e) {
            log.error("连接客户端时有错误发生", e);
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        channels.put(key, channel);
        return channel;
    }

    /**
     * 建立连接
     *
     * @param bootstrap
     * @param instanceNode
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private static Channel connect(Bootstrap bootstrap, InstanceNode instanceNode) throws ExecutionException, InterruptedException {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(new InetSocketAddress(instanceNode.getHost(), instanceNode.getPort())).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端连接成功!");
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get(); // 阻塞，直到连接建立成功
    }

    /**
     * 初始化bootstrap
     *
     * @return
     */
    private static Bootstrap initializeBootstrap() {
        System.out.println("init bootstrap");
        Bootstrap bootstrap = new Bootstrap();
        //日志handler
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        // 编解码器
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        //处理相应handler
        RpcResponseHandler RPC_HANDLER = new RpcResponseHandler();
        //心跳处理器
        HeartBeatClientHandler HEARTBEAT_CLIENT = new HeartBeatClientHandler();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                 //连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                 //是否开启 TCP 底层心跳机制
                 // .option(ChannelOption.SO_KEEPALIVE, true)
                 //TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                 // .option(ChannelOption.TCP_NODELAY, true)
                 .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) throws Exception {
                         ch.pipeline()
                           // 空闲检测处理器
                           .addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
                           //定长解码器
                           .addLast(new ProcotolFrameDecoder())
                           // 编解码器
                           .addLast(MESSAGE_CODEC)
                           // 日志处理器
                           .addLast(LOGGING_HANDLER)
                           // 发送心跳包的处理器，维持与与服务端的连接
                           .addLast(HEARTBEAT_CLIENT)
                           .addLast(RPC_HANDLER);
                     }
                 });
        return bootstrap;
    }

}
