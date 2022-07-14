package com.moon.rpc.transport.protocol;

import com.moon.rpc.transport.constant.ProtocolConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.io.Serializable;

/**
 * @author mzx
 * @date 2022/7/13 14:43
 * 协议的消息头
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageHeader implements Serializable {

    /*
    +---------------------------------------------------------------+
    | 魔数 4byte | 协议版本号 1byte | 序列化算法 1byte | 消息类型 1byte  |
    +---------------------------------------------------------------+
    | 消息ID 4byte      |  对齐填充 1byte  |        数据长度 4byte     |
    +---------------------------------------------------------------+
    */
    //对齐填充位置和消息长度没必要写在这个HEADER里面，而是在编解码器处手动写入

    /**
     *  魔数
     */
    private int magic;

    /**
     *  协议版本号
     */
    private byte version;

    /**
     *  序列化器
     */
    private byte serializer;

    /**
     *  消息类型
     */
    private byte msgType;

    /**
     *  消息 ID
     */
    private int requestId;


    public static MessageHeaderBuilder builder() {
        return new MessageHeaderBuilder();
    }

    public static class MessageHeaderBuilder {
        /**
         * 设置初始默认值
         */
        private int magic = ProtocolConstant.MAGIC;
        private byte version = ProtocolConstant.VERSION;
        private byte serializer = ProtocolConstant.SERIALIZER.getType();
        private byte msgType;
        private int requestId;

        public MessageHeaderBuilder magic(int magic) {
            this.magic = magic;
            return this;
        }

        public MessageHeaderBuilder version(byte version) {
            this.version = version;
            return this;
        }

        public MessageHeaderBuilder serializer(byte serializer) {
            this.serializer = serializer;
            return this;
        }

        public MessageHeaderBuilder msgType(byte msgType) {
            this.msgType = msgType;
            return this;
        }

        public MessageHeaderBuilder requestId(int requestId) {
            this.requestId = requestId;
            return this;
        }


        public MessageHeader build() {
            return new MessageHeader(this.magic, this.version, this.serializer, this.msgType, this.requestId);
        }
    }
}
