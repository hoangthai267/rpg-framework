package com.rpg.framework.sever;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.*;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import io.netty.channel.socket.nio.*;

public class SocketServer {
	private String 			host;
    private int 			port;
    private ServerBootstrap bootstrap;
    private EventLoopGroup 	bossGroup, workerGroup;
    private int				numberOfThread;
    
    public SocketServer() {
        bootstrap = new ServerBootstrap()
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);
    }

    public ServerBootstrap getBootstrap() {
        return bootstrap;
    }

    public synchronized boolean start(String host, int port) throws Exception {
        if (bossGroup != null || workerGroup != null)
        {
            return false;
        }

        this.host 			= host;
        this.port 			= port;
        this.numberOfThread = Runtime.getRuntime().availableProcessors() << 2;        
        this.bossGroup 		= new NioEventLoopGroup();
        this.workerGroup 	= new NioEventLoopGroup(numberOfThread);

        bootstrap.group(bossGroup, workerGroup)
                .childHandler(new SocketServerInitializer())
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
        return (bossGroup != null && bossGroup.isShuttingDown())
            || (workerGroup != null && workerGroup.isShuttingDown());
    }
}
