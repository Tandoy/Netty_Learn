package com.tz.netty.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;

/**
 * Netty 服务器在 6668 端口监听，浏览器发出请求 http://localhost:8023/
 * 服务器可以回复消息给客户端"Hello!我是服务器5",并对特定请求资源进行过滤。
 */
public class TestServer {
    public static void main(String[] args) {
        // 创建bossGroup、workerGroup
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)//用来给接收到的通道添加配置
                    .childHandler(new ChannelInitializer<SocketChannel>() { //该方法用来设置业务处理类（自定义的handler）
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 当workerGroup监听到活跃事件后，会放到对应的pipeline中处理，其实pipeline中也是由多个handler组成进行业务逻辑处理
                            // 得到pipeline
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //加入一个netty 提供的httpServerCodec codec =>[coder - decoder]
                            //HttpServerCodec 说明
                            //1. HttpServerCodec 是netty 提供的处理http的 编-解码器
                            pipeline.addLast("MyHttpServerCodec", new HttpServerCodec());
                            //2. 增加一个自定义的handler
                            pipeline.addLast("MyTestHttpServerHandler", new TestHttpServerHandler());
                        }
                    });

            // 绑定端口
            ChannelFuture future = serverBootstrap.bind(8023).sync();

            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        System.out.println("监听端口 8023 成功");
                    } else {
                        System.out.println("监听端口 8023 失败");
                    }
                }
            });
            // 对关闭通道进行监听
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace().toString());
        } finally {
            // 关闭资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
