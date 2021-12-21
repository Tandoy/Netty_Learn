package com.tz.netty.sub;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * TCP粘包 拆包实例程序
 */
public class SubClient {
    private final String host;
    private final int port;

    public SubClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //加入相关handler
                            pipeline.addLast("decoder", new MyMessageEncoder());
                            pipeline.addLast("encoder", new MyMessageDecoder());
                            //加入自定义的handler
                            pipeline.addLast(new MyClientHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            //得到channel
            Channel channel = channelFuture.channel();
            //给关闭通道进行监听
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            System.out.println(e.fillInStackTrace().toString());
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new SubClient("127.0.0.1", 8024).run();
    }
}
