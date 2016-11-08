package com.rpg.framework.sever;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class SocketServerInitializer extends ChannelInitializer<SocketChannel> {
	public final static String PIPELINE_IDLE = "idle";
	public final static String PIPELINE_DECODER = "decoder";
	public final static String PIPELINE_HANDLER = "handler";

	/**
	 * In seconds
	 */
	public final static int IDLE_TIME_READER = 60 * 60; // second
	/**
	 * In seconds
	 */
	public final static int IDLE_TIME_WRITER = 60 * 60; // second
	/**
	 * In seconds
	 */
	public final static int IDLE_TIME_ALL = 60 * 60; // second

	private SocketServerManager manager;
	
	public SocketServerInitializer(SocketServerManager manager) {
		this.manager = manager;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();		
		p.addLast(PIPELINE_IDLE, new IdleStateHandler(IDLE_TIME_READER, IDLE_TIME_WRITER, IDLE_TIME_ALL));
		p.addLast(PIPELINE_DECODER, new SocketServerDecoder(manager));
		p.addLast(PIPELINE_HANDLER, new SocketServerHandler(manager));
	}
}
