package com.rpg.framework.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Timer;
import java.util.TimerTask;

public class SocketClient {
	private Bootstrap bootstrap;
	private EventLoopGroup group;
	private Channel channel;
	private SocketClientHandler handler;

	public SocketClient(String host, int port) {
		try {
			group = new NioEventLoopGroup();
			bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class)
					.handler(new SocketClientInitializer(this));

			// Make a new connection.
			channel = bootstrap.connect(host, port).sync().channel();

			// Get the handler instance to initiate the request.
			handler = channel.pipeline().get(SocketClientHandler.class);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		System.out.println("Client start");		
		new Timer().scheduleAtFixedRate(new TimerTask() {			
			@Override
			public void run() {
				handler.update();
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
	
	public void send(int commandID, byte[] data) {
		handler.send(commandID, data);
	}
	
	public void receive(int commandID, byte[] data) {
		
	}
}
