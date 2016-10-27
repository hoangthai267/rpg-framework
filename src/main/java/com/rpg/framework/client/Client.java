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
import java.util.Timer;
import java.util.TimerTask;

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
				sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
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

	public void start() {
		System.out.println("Client start");		
//		for(int i = 801; i <= 1000; i++) {			
//			handler.requestRegister("admin" + i, "admin");
//		}
		handler.requestLogin("admin1", "admin");
		handler.requestListOfCharacter("User_1");
		handler.requestCreateCharacter("User_1", "WA", "Warrior");
		handler.requestListOfCharacter("User_1");
		handler.requestStartGame("User_1", "Character_1");
		new Timer().scheduleAtFixedRate(new TimerTask() {			
			@Override
			public void run() {
				handler.handleRequest();
				if(handler.isRunning() == false) {
					stop();
					System.exit(0);
				}
			}
		}, 0, 33);
	}

	public boolean running() {
		return handler.isRunning();
	}

	public void stop() {
		channel.close();
		group.shutdownGracefully();
		System.out.println("Client Stop");
	}

	public static void main(String args[]) {
		int count = 0;
		Client client = new Client();
		client.start();
	}
}
