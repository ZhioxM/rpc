package com.moon.rpc.transport.codec;


import com.moon.rpc.transport.constant.MessageClassFactory;
import com.moon.rpc.transport.constant.ProtocolConstant;
import com.moon.rpc.transport.dto.Message;
import com.moon.rpc.transport.serializer.RpcSerializer;
import com.moon.rpc.transport.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author mzx
 */
@Slf4j
@ChannelHandler.Sharable
/**
 * 必须和 LengthFieldBasedFrameDecoder 一起使用，确保接到的 ByteBuf 消息是完整的
 */
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {
    // OutBound时调用
    @Override
    // 自定义协议 ： 编解码的规则
    public void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 1.  4字节的魔数
        out.writeInt(ProtocolConstant.MAGIC);
        // 2. 1字节的版本
        out.writeByte(ProtocolConstant.VERSION);
        // 3.  1字节的序列化方式
        out.writeByte(ProtocolConstant.SERIALIZER.getType());
        // 4. 1字节的指令类型
        out.writeByte(msg.getMessageType());
        // 5. 4个字节的序号
        out.writeInt(msg.getSequenceId());
        // 无意义，对齐填充c
        out.writeByte(0xff);
        // 6. 获取内容的字节数组
        RpcSerializer rpcSerializer = SerializerFactory.getRpcSerializer(ProtocolConstant.SERIALIZER.getType());
        byte[] bytes =rpcSerializer.serialize(msg);
        // 7. 长度
        out.writeInt(bytes.length);
        // 8. 写入内容
        out.writeBytes(bytes);
        outList.add(out);
    }

    // Inbound时用
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int magicNum = in.readInt();
        byte version = in.readByte();
        byte serializerType = in.readByte();
        byte messageType = in.readByte();
        int sequenceId = in.readInt();
        in.readByte(); // 填充位
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes, 0, length);
        // 获取序列化器）
        RpcSerializer serializer = SerializerFactory.getRpcSerializer(serializerType);
        // 反序列化
        Message message = serializer.deserialize(bytes, MessageClassFactory.get(messageType));
        //log.debug("{}", message);
        //in.release(); // 可以释放了吧？，因为他都被转化成Message对象了 --> 根据Netty In Action p127，不需要我们显示的释放，编解码器在解码或者编码后会自动帮我们释放一次
        out.add(message);
    }

}
