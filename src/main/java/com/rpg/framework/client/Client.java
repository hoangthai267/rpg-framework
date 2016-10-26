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

import javax.net.ssl.SSLException;

public class Client {
	static final boolean SSL = System.getProperty("ssl") != null;
	static final String HOST = System.getProperty("host", "127.0.0.1");
	static final int PORT = Integer.parseInt(System.getProperty("port", "8463"));

	private Bootstrap bootstrap;
	private EventLoopGroup group;
	private SslContext sslCtx;
	private Channel channel;
	private ClientHandler handler;

	public Client() {
		try {
			if (SSL) {
				sslCtx = SslContextBuilder
						.forClient()
						.trustManager(InsecureTrustManagerFactory.INSTANCE)
						.build();
			} else {
				sslCtx = null;
			}

			group = new NioEventLoopGroup();
			bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class)
					.handler(new ClientInitializer(sslCtx));

			// Make a new connection.
			channel = bootstrap.connect(HOST, PORT).sync().channel();

			// Get the handler instance to initiate the request.
			handler = channel.pipeline().get(ClientHandler.class);

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (SSLException e) {
			e.printStackTrace();
		}
	}

	public synchronized void start() throws Exception {
		handler.registerUser();
	}

	public static void main(String args[]) {
		try {
			new Client().start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
