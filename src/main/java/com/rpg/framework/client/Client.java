package com.rpg.framework.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.util.Arrays;
import java.util.List;

public class Client {
    static final boolean SSL = System.getProperty("ssl") != null;
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8463"));

    public synchronized void start(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ClientInitializer(sslCtx));

            // Make a new connection.
            Channel ch = b.connect(HOST, PORT).sync().channel();

            // Get the handler instance to initiate the request.
            ClientHandler handler = ch.pipeline().get(ClientHandler.class);
            
            handler.registerUser();
//            
//            // Close the connection.
//            ch.close();
            
        } finally {
            //group.shutdownGracefully();
        }
    }
    
    public static void main(String args[]) {
    	try {
			new Client().start(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
