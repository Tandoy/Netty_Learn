package com.tz.netty.rpc.provider;

import com.tz.netty.rpc.netty.RpcNettyServer;

public class RpcServerBootstrap {
    public static void main(String[] args) {
        // 启动netty服务端
        RpcNettyServer.startServer("127.0.0.1", 7000);
    }
}
