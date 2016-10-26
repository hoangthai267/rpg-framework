package com.rpg.framework.sever;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class SocketServer {
	private String host;
    private int port;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup, workerGroup;

    public static int s_num_worker_thread = Runtime.getRuntime().availableProcessors() << 2;
    public SocketServer() {
        bootstrap = new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)  //default from OS .
                .childOption(ChannelOption.TCP_NODELAY, true);
    }

    public ServerBootstrap getBootstrap() {
        return bootstrap;
    }

    public synchronized boolean start(String host, int port, ChannelInitializer<SocketChannel> socketServerInitializer) throws Exception {
        if (bossGroup != null || workerGroup != null)
        {
            return false;
        }

        this.host = host;
        this.port = port;
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup( s_num_worker_thread);

        bootstrap.group(bossGroup, workerGroup)
                .childHandler(socketServerInitializer)
                .bind(Address.getInetSocketAddress(host, port))
                .sync();

        return true;
    }

    public String GetHost() {
        return host;
    }

    public int GetPort() {
        return port;
    }
    

    public synchronized boolean stop() {
        return stop(100, 15000, TimeUnit.MILLISECONDS);
    }

    public synchronized boolean stop(int quietPeriod, int timeout, TimeUnit unit) {
        if (bossGroup == null || workerGroup == null)
        {
            return false;
        }

        bossGroup.shutdownGracefully(quietPeriod, timeout, unit);
        bossGroup = null;

        workerGroup.shutdownGracefully(quietPeriod, timeout, unit);
        workerGroup = null;
        return true;
    }
    public boolean IsShuttingDown()
    {
        return (bossGroup!=null && bossGroup.isShuttingDown())
            || (workerGroup!=null && workerGroup.isShuttingDown());
    }
//    static final boolean SSL = System.getProperty("ssl") != null;
//    static final int PORT = Integer.parseInt(System.getProperty("port", "8463"));
//
//    public static void main(String[] args) throws Exception {
//        // Configure SSL.
//        final SslContext sslCtx;
//        if (SSL) {
//            SelfSignedCertificate ssc = new SelfSignedCertificate();
//            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
//        } else {
//            sslCtx = null;
//        }
//
//        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
//        EventLoopGroup workerGroup = new NioEventLoopGroup();
//        try {
//            ServerBootstrap b = new ServerBootstrap();
//            b.group(bossGroup, workerGroup)
//             .channel(NioServerSocketChannel.class)
//             .handler(new LoggingHandler(LogLevel.INFO))
//             .childHandler(new ServerInitializer(sslCtx));
//
//            b.bind(PORT).sync().channel().closeFuture().sync();
//        } finally {
//            bossGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
//        }
//    }
}
