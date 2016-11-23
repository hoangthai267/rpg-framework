package com.rpg.framework.core;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;

public class Server {
	private final static String PIPELINE_IDLE 		= "idle";
	private final static String PIPELINE_DECODER 	= "decoder";
	private final static String PIPELINE_HANDLER 	= "handler";

	private final static int 	IDLE_TIME_READER 	= 60 * 60; // second
	private final static int 	IDLE_TIME_WRITER	= 60 * 60; // second
	private final static int 	IDLE_TIME_ALL 		= 60 * 60; // second
	
	private final static int 	HEADER_LEN 			= 6;
	private final static int 	MAX_PACKAGE_SIZE 	= 1024 * 64;
	
	private final static int 	TARGET_FPS 			= 60;
	private final static long 	OPTIMAL_TIME 		= 1000000000 / TARGET_FPS;
	
	private int 			numberOfThread;
	private boolean			running;
	private ServerBootstrap bootstrap;
	private EventLoopGroup 	bossGroup;
	private EventLoopGroup 	workerGroup;
	private ChannelFuture 	channelFuture;
	
	private AtomicInteger	numberOfConnection;
	private Map<Integer, ChannelHandlerContext> channels;	
	private List<Integer> 	writingChannels;
	
	public Server() {
		this.numberOfThread 	= Runtime.getRuntime().availableProcessors() << 2;
		this.bossGroup 			= new NioEventLoopGroup();
		this.workerGroup 		= new NioEventLoopGroup(numberOfThread);
		this.numberOfConnection = new AtomicInteger(0);
		this.channels			= new HashMap<Integer, ChannelHandlerContext>();
		this.writingChannels	= new LinkedList<Integer>();
		this.running			= true;
	}
	
	public boolean initialize() {
		this.bootstrap = new ServerBootstrap()
				.channel(NioServerSocketChannel.class)
				.childOption(ChannelOption.SO_KEEPALIVE, true)
				.childOption(ChannelOption.TCP_NODELAY, true)
				.group(bossGroup, workerGroup)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						p.addLast(PIPELINE_IDLE,
								new IdleStateHandler(IDLE_TIME_READER, IDLE_TIME_WRITER, IDLE_TIME_ALL));
						p.addLast(PIPELINE_DECODER, new ByteToMessageDecoder() {

							@Override
							protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
									throws Exception {
								if (in.readableBytes() <= HEADER_LEN)
									return;

								in.markReaderIndex();
								int bodyLen = in.readInt() - 4;

								if (bodyLen <= 0 || bodyLen > MAX_PACKAGE_SIZE) {
									System.out.println("Message is too big");
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
							private int channelID;

							@Override
							public void channelActive(ChannelHandlerContext ctx) throws Exception {
								channelID = numberOfConnection.incrementAndGet();
								channels.put(channelID, ctx);
								connectedClient(channelID);
							}

							@Override
							public void channelInactive(ChannelHandlerContext ctx) throws Exception {
								numberOfConnection.decrementAndGet();
								channels.remove(channelID);
								disconnectedClient(channelID);
							}

							@Override
							public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
								ByteBuf data = (ByteBuf) msg;
								try {
									int messageID = data.readShort();
									byte[] buffer = new byte[data.capacity() - 2];
									data.getBytes(2, buffer);

									receiveMessageFrom(channelID, messageID, buffer);
								} finally {
									data.release();
								}
							}
							
							@Override
							public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//								System.out.println("exceptionCaught: " + cause.getMessage());
							}
						});

					}
				});
		
		
		return true;
	}
	
	public void start(String host, int port) {
		try {
			this.channelFuture = this.bootstrap.bind(new InetSocketAddress(host, port)).sync();			
		} catch (Exception ex) {
			running = false;
			ex.printStackTrace();
		} finally {
			System.out.println("Server start at host " + host + " port " + port);
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
				// System.out.println("FPS: " + fps);
				lastFpsTime = 0;
				fps = 0;
			}

			// update the server logic
			update(delta);

			// draw everyting
			// render();
		}
	}
	
	public void update(double delta) {
		
	}
	
	public void stop() {
		try {
			this.channelFuture.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
			running = false;
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
	
	public void connectedClient(int clientID) {
	}
	
	public void disconnectedClient(int clientID) {
	}
	
	public void receiveMessageFrom(int clientID, int messageID, byte[] data) {

	}
	
	public boolean sendMessageTo(final int clientID, int messageID, byte[] data) {
		ChannelHandlerContext channel = channels.get(clientID);

		if (channel == null)
			return true;
		
//		if(writingChannels.contains(clientID))
//			return false;
//		
//		writingChannels.add(clientID);
		
		ByteBuf respBuf = channel.alloc().buffer();
		int size = data.length + 6;
		short flag = 0;

		respBuf.clear();
		respBuf.writeInt(size);
		respBuf.writeShort(messageID);
		respBuf.writeBytes(data);
		
		channel.writeAndFlush(respBuf).addListener(new ChannelFutureListener() {

			public void operationComplete(ChannelFuture future) throws Exception {
//				writingChannels.remove((Integer)clientID);
			}
			
		});
		
		return true;
	}
	
	public void sendMessageToList(List<Integer> clients, int messageID, byte[] data) {
		for (Integer id : clients) {
			ChannelHandlerContext channel = channels.get(id);
			if (channel == null)
				return;
			ByteBuf respBuf = channel.alloc().buffer();
			int size = data.length + 6;
			short flag = 0;

			respBuf.clear();
			respBuf.writeInt(size);
			respBuf.writeShort(messageID);
			respBuf.writeBytes(data);

			channel.writeAndFlush(respBuf).addListener(new ChannelFutureListener() {

				public void operationComplete(ChannelFuture future) throws Exception {

				}
				
			});
		}
	}
	
	public void sendMessageToAll(int messageID, byte[] data) {
		for (ChannelHandlerContext channel : channels.values()) {
			if (channel == null)
				return;
			ByteBuf respBuf = channel.alloc().buffer();
			int size = data.length + 6;
			short flag = 0;

			respBuf.clear();
			respBuf.writeInt(size);
			respBuf.writeShort(messageID);
			respBuf.writeBytes(data);

			channel.writeAndFlush(respBuf).addListener(new ChannelFutureListener() {

				public void operationComplete(ChannelFuture future) throws Exception {
					
				}
				
			});
		}
	}
}
