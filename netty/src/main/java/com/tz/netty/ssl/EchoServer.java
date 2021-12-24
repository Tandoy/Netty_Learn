package com.tz.netty.ssl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public final class EchoServer {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        // Configure the server.

        /**
         * (1) 这两个对象是整个 Netty 的核心对象，可以说，整个 Netty 的运作都依赖于他们。bossGroup 用于接受 TCP 请求，他会将请求交给 workerGroup，workerGroup 会获取到真正的连接，然后和连接进行通信，比如读写解码编码等操作。
         * (2) EventLoopGroup 是事件循环组（线程组）含有多个 EventLoop，可以注册 channel，用于在事件循环中去进行选择（和选择器相关）。
         * (3) new NioEventLoopGroup(1); 这个 1 表示 bossGroup 事件组有 1 个线程可以指定，如果 new NioEventLoopGroup() 会含有默认个线程 cpu核数 * 2，即可以充分的利用多核的优势
         *
         * @param nThreads 使用的线程数，默认为 core * 2【可以追踪源码】
         * @param executor 执行器:如果传入 null, 则采用 Netty 默认的线程工厂和默认的执行器 ThreadPerTaskExecutor
         * @param chooserFactory 单例 new DefaultEventExecutorChooserFactory()
         * @param args args 在创建执行器的时候传入固定参数
         *
         *
         * 创建步骤：
         *           1.如果 executor 是 null，创建一个默认的 ThreadPerTaskExecutor，使用 Netty 默认的线程工厂。
         *           2.根据传入的线程数（CPU * 2）创建一个线程池（单例线程池）数组。
         *           3.循环填充数组中的元素。如果异常，则关闭所有的单例线程池。
         *           4.根据线程选择工厂创建一个线程选择器。
         *           5.为每一个单例线程池添加一个关闭监听器。
         *           6.将所有的单例线程池添加到一个 HashSet 中。
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc()));
                            }
                            //p.addLast(new LoggingHandler(LogLevel.INFO));
                            p.addLast(new EchoServerHandler());
                        }
                    });

            // Start the server.

            /**
             * 这里源码核心是两个方法 initAndRegister 和 doBind0
             *  todo 未完待续
             */
            ChannelFuture f = b.bind(PORT).sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
