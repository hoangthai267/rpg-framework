package com.rpg.framework.sever;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class SocketServerHandler extends ChannelInboundHandlerAdapter {
	private AtomicInteger numberConnection;
	private ChannelHandlerContext channelHandlerContext;
	private SocketServer socketServer;
	
	public SocketServerHandler(SocketServer server) {
		super();		
		this.numberConnection = new AtomicInteger();
		this.socketServer = server;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		numberConnection.incrementAndGet();
		this.channelHandlerContext = ctx;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		numberConnection.decrementAndGet();
		channelHandlerContext = null;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println(ctx.channel().toString());
		ByteBuf data = (ByteBuf) msg;
		byte[] buffer = new byte[data.capacity() - 2];
		data.getBytes(2, buffer);
		receive(data.readShort(), buffer);
	}

	public void HandleExceptionContext(ChannelHandlerContext ctx, Throwable Cause) throws Exception {
		if (Cause.getMessage().startsWith("An existing connection was forcibly")
				|| Cause.getMessage().startsWith("Connection reset by peer")
				|| Cause.getMessage().startsWith("Connection timed out")) {
			close(ctx);
		} else {
			close(ctx);
			Cause.printStackTrace();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		HandleExceptionContext(ctx, cause);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		super.userEventTriggered(ctx, evt);

		if (evt instanceof IdleStateEvent) {
			// IdleStateEvent idleEvent = (IdleStateEvent)evt;

			close(ctx);
		}
	}

	public void removeIdleTime() {
		ChannelPipeline pipeline = channelHandlerContext.pipeline();
		if (pipeline.get(SocketServerInitializer.PIPELINE_IDLE) != null)
			pipeline.remove(SocketServerInitializer.PIPELINE_IDLE);
	}

	public void setIdleTime(int idleTimeReader, int idleTimeWriter, int idleTimeAll) {
		removeIdleTime();
		channelHandlerContext.pipeline().addFirst(SocketServerInitializer.PIPELINE_IDLE,
				new IdleStateHandler(idleTimeReader, idleTimeWriter, idleTimeAll, TimeUnit.MILLISECONDS));
	}

	public void writeAndFlush(int cmd, int flag, byte[] data) {
		int len = data.length;
		ByteBuf sendBuf = channelHandlerContext.alloc().buffer(len + SocketServerDecoder.HEADER_LEN);
		sendBuf.writeInt(len + 4).writeInt(cmd).writeShort(flag).writeBytes(data);
		channelHandlerContext.writeAndFlush(sendBuf);
	}

	public int getNumConnection() {
		return numberConnection.get();
	}

	private void close(ChannelHandlerContext ctx) {
		ctx.close();
		System.out.println("Channel shutdown");
	}
	
	public void send(int commandID, byte[] data) {
		ByteBuf respBuf = channelHandlerContext.alloc().buffer();
		int size = data.length + 8;
		short flag = 0;

		respBuf.clear();
		respBuf.writeInt(size);
		respBuf.writeShort(flag);
		respBuf.writeShort(commandID);
		respBuf.writeBytes(data);

		channelHandlerContext.writeAndFlush(respBuf);
	}
	
	public void receive(int commandID, byte[] data) {
		byte[] result = handleMessage(commandID, data);
		send(commandID + 1, result);
	}
	
	public byte[] handleMessage(int commandID, byte[] data) {
		return socketServer.handleMessage(commandID, data);
	}
}
