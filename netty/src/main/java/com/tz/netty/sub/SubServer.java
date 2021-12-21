package com.tz.netty.sub;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * TCP粘包 拆包实例程序
 */
public class SubServer {
    public int port;

    public SubServer(int port) {
        this.port = port;
    }

    public void run() {
        // 1.创建两个线程组 boss/worker
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 2.创建服务器端引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // 3.为引导程序进行配置
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class) // 服务端channel
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 4.获取pipeline
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //加入相关handler
                            pipeline.addLast("decoder", new MyMessageEncoder());
                            pipeline.addLast("encoder", new MyMessageDecoder());
                            // 5.根据业务逻辑添加链式handler
                            pipeline.addLast(new MyServerHandler());
                        }
                    });

            // 6.start
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            // 7.添加异步监听
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isSuccess()) {
                        System.out.println("netty server is started....");
                    }else {
                        System.out.println("some thing is wrong....");
                    }
                }
            });

            // 7.关闭监听
            // 注意：如果此处不加同步阻塞sync，netty服务端会启动后又无异常地自动退出
            // 解决方式：
            // 1.加上同步阻塞sync,那么只有服务端正常关闭channel时才会执行finally里的语句
            // 2.把finally里的语句移到operationComplete里面，那么也只有channel关闭时才会让netty的两个线程组关闭
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e) {
            System.out.println(e.fillInStackTrace().toString());
        }finally {
            // 6.关闭资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new SubServer(8024).run();
    }
}
