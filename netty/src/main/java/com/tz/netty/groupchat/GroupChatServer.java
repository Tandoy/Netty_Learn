package com.tz.netty.groupchat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 编写一个 Netty 群聊系统，实现服务器端和客户端之间的数据简单通讯（非阻塞）
 * 实现多人群聊
 * 服务器端：可以监测用户上线，离线，并实现消息转发功能
 * 客户端：通过 channel 可以无阻塞发送消息给其它所有用户，同时可以接受其它用户发送的消息（有服务器转发得到）
 */
public class GroupChatServer {
    private int port;

    public GroupChatServer(int port) {
        this.port = port;
    }

    public void run() {
        // 1.创建两个线程组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 2.创建服务器端引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // 3.进行相关配置
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            // 4.获取pipeline
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // 5.根据业务逻辑添加链式handler
                            pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast(new GroupChatServerHandler());
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            // 6.启动监听
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) {
                    if (channelFuture.isSuccess()) {
                        System.out.println("netty server is started....");
                    } else {
                        System.out.println("some thing is wrong....");
                    }
                }
            });

            // 7.关闭监听
            // 注意：如果自处不加同步阻塞sync，netty服务端会启动后又无异常地自动退出
            // 解决方式：
                // 1.加上同步阻塞sync,那么只有服务端正常关闭channel时才会执行finally里的语句
                // 2.把finally里的语句移到operationComplete里面，那么也只有channel关闭时才会让netty的两个线程组关闭
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace().toString());
        } finally {
            // 6.关闭资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        // 启动服务端
        new GroupChatServer(8023).run();
    }
}
