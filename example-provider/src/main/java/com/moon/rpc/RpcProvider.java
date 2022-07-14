package com.moon.rpc;

import com.moon.rpc.server.RpcServer;
import com.moon.rpc.server.annotation.RpcServiceScan;

/**
 * @author mzx
 * @date 2022/7/14 14:55
 */
@RpcServiceScan
// 注意注解使用的包路径的位置
public class RpcProvider {
    public static void main(String[] args) {
        new RpcServer("localhost", 8080).start();
    }
}
