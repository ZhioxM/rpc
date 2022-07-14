package com.moon.rpc.transport.dto;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author mzx
 * @date 2022/7/14 15:38
 */
@Data
@ToString
public abstract class Message implements Serializable {
    protected int sequenceId;
    protected byte messageType;
}
