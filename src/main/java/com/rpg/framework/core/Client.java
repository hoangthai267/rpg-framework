package com.rpg.framework.core;

import java.net.InetSocketAddress;
import java.util.List;

import com.couchbase.client.deps.io.netty.util.ReferenceCountUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateHandler;

public class Client {
	private final static String PIPELINE_IDLE 		= "idle";
	private final static String PIPELINE_DECODER 	= "decoder";
	private final static String PIPELINE_HANDLER 	= "handler";

	private final static int 	IDLE_TIME_READER 	= 60 * 60; // second
	private final static int 	IDLE_TIME_WRITER 	= 60 * 60; // second
	private final static int 	IDLE_TIME_ALL 		= 60 * 60; // second

	private final static int 	HEADER_LEN 			= 4;
	
	private final static int 	TARGET_FPS 			= 60;
	private final static long 	OPTIMAL_TIME 		= 1000000000 / TARGET_FPS;
	
	private Bootstrap 		bootstrap;
	private EventLoopGroup 	group;
	private Channel			channel;
	private boolean			running;
	public Client() {
		 group 		= new NioEventLoopGroup();
		 bootstrap 	= new Bootstrap();
		 running	= true;
	}

	public boolean initialize() {
		bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline p = ch.pipeline();
				p.addLast(PIPELINE_IDLE, new IdleStateHandler(IDLE_TIME_READER, IDLE_TIME_WRITER, IDLE_TIME_ALL));
				
				p.addLast(PIPELINE_DECODER, new ByteToMessageDecoder() {
					
					@Override
					protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
						if (in.readableBytes() <= HEADER_LEN) // 4 byte len, x byte data
						{
							return;
						}
						
						in.markReaderIndex();
						int bodyLen = in.readInt() - 4;
						
						if (bodyLen <= 0) {
							ctx.close();
							return;
						}
						
						if (bodyLen > in.readableBytes()) {
							in.resetReaderIndex();
							return;
						}
						
						out.add(in.readBytes(bodyLen));
					}
				});
				
				p.addLast(PIPELINE_HANDLER, new ChannelInboundHandlerAdapter() {
					
					@Override
					public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
						channel = ctx.channel();
						running = true;
					}
					
					@Override
					public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
						channel = null;
						running = false;
					}
					
					@Override
					public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
						ByteBuf data = (ByteBuf) msg;
						try {
							int messageID = data.readShort();
							byte[] buffer = new byte[data.capacity() - 2];
							data.getBytes(2, buffer);

							receiveMessage(messageID, buffer);
						} finally {
							data.release();
						}
					}
					
					@Override
					public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//						cause.printStackTrace();
					}
					
				});
			}
			
		});
		
		return true;
	}

	public void start(String host, int port) {
		try {
			bootstrap.connect(new InetSocketAddress(host, port)).sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Client connect at host " + host + " port " + port + ".");
		}
		
		long lastLoopTime = System.nanoTime();
		long lastFpsTime = 0;
		int fps = 0;

		while (running) {
			long now = System.nanoTime();
			long updateLength = now - lastLoopTime;
			lastLoopTime = now;

			if (updateLength < OPTIMAL_TIME) {
				long sleepTime = (OPTIMAL_TIME - updateLength) / 1000000;
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				updateLength = OPTIMAL_TIME;
			}

			double delta = updateLength / ((double) 1000000000);
			// update the frame counter
			lastFpsTime += updateLength;
			fps++;

			// update our FPS counter if a second has passed since we last recorded
			if (lastFpsTime >= 1000000000) {
				updatePerSecond(delta, fps);
				lastFpsTime = 0;
				fps = 0;
			}

			// update the server logic
			update(delta);

			// draw everyting
			// render();
		}
		
	}
	
	public void stop() {
		try {
			running = false;			
			group.shutdownGracefully().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Client disconnect.");
		}
	}
	
	public void update(double delta) {
		
	}

	public void receiveMessage(int commandID, byte[] data) {
		
	}
	
	public void sendMessage(int commandID, byte[] data) {
		if(channel == null)
			return;
		
		ByteBuf respBuf = channel.alloc().buffer();		
		int size = data.length + 6;

		respBuf.clear();
		respBuf.writeInt(size);
		respBuf.writeShort(commandID);
		respBuf.writeBytes(data);

		channel.writeAndFlush(respBuf).addListener(new ChannelFutureListener() {

			public void operationComplete(ChannelFuture future) throws Exception {

			}
			
		});
		
		ReferenceCountUtil.release(respBuf);
	}

	public void updatePerSecond(double delta, int fps) {
	
	}
	
}
