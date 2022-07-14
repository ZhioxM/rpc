package com.moon.rpc.transport.dto;

import com.moon.rpc.transport.constant.MessageType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author mzx
 */
@Getter
@Setter
public class HeartBeat extends Message{

    public HeartBeat() {
        this.messageType = MessageType.HEARTBEAT.getType();
    }

    @Override
    public String toString() {
        return "HeartBeat{" +
                "sequenceId=" + sequenceId +
                ", messageType=" + messageType +
                '}';
    }
}
