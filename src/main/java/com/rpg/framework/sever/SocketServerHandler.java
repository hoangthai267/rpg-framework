package com.rpg.framework.sever;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rpg.framework.data.CouchBase;
import com.rpg.framework.data.Protocol;
import com.rpg.framework.data.Protocol.ResponseCode;
import com.rpg.framework.handler.*;
import com.rpg.framework.util.*;

public class SocketServerHandler extends ChannelInboundHandlerAdapter {
	private static final AtomicInteger numConnection = new AtomicInteger();
	private static final String s_defaultRemoteAddress = "";
	private static final String s_defaultUserId = "";

	private UserHandler m_user;
	private ChannelHandlerContext channelHandlerContext;

	private String m_userId;
	private String m_remoteAddress;
	private int m_delayLoginCount;
	private long m_secFirstDelayLogin;

	public SocketServerHandler() {
		super();
		m_user = null;
		m_remoteAddress = s_defaultRemoteAddress;
		m_userId = s_defaultUserId;
		m_delayLoginCount = 0;
		m_secFirstDelayLogin = Time.currentTimeSecond();
	}

	public String GetUserId() {
		return m_userId;
	}

	public void IncreaseDelayLogin() {
		if (m_delayLoginCount == 0) {
			m_secFirstDelayLogin = Time.currentTimeSecond();
		}
		m_delayLoginCount++;
	}

	public int GetDelayLoginCount() {
		return m_delayLoginCount;
	}

	public long GetSecFirstDelayLogin() {
		return m_secFirstDelayLogin;
	}

	public void ResetDelayLogin() {
		m_secFirstDelayLogin = 0;
		m_delayLoginCount = 0;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		numConnection.incrementAndGet();
		this.channelHandlerContext = ctx;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		numConnection.decrementAndGet();
		m_user = null;
		channelHandlerContext = null;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf data = (ByteBuf) msg;
		int commandID = data.readShort();
		
		if (m_user != null) {
			m_user.handleRequest(commandID, data);
		} else {
			handleRequest(commandID, data);
		}
	}

	public void HandleExceptionContext(ChannelHandlerContext ctx, Throwable Cause) throws Exception {
		if (Cause.getMessage().startsWith("An existing connection was forcibly")
				|| Cause.getMessage().startsWith("Connection reset by peer")
				|| Cause.getMessage().startsWith("Connection timed out")) {
			CloseSocket(ctx);
		} else {
			CloseSocket(ctx);
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

			CloseSocket(ctx);
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

	public static int getNumConnection() {
		return numConnection.get();
	}

	public String getRemoteAddress() {
		return m_remoteAddress;
	}

	public void SetUser(UserHandler AUser) {
		if (AUser != null) {
			m_user = AUser;
			m_user.m_handlerSocket = this;
			m_user.m_channel = this.channelHandlerContext;

			m_userId = AUser.getUserID();
			m_remoteAddress = this.channelHandlerContext.channel().remoteAddress() != null
					? this.channelHandlerContext.channel().remoteAddress().toString() : "contextNullAddress";
		}
	}

	public void RemoveUser() {
		m_user = null;
	}

	public UserHandler GetUser() {
		return m_user;
	}

	private void CloseSocket(ChannelHandlerContext ctx) {
		ctx.close();
		System.out.println("Channel shutdown");
	}

	public void handleRequest(int commandID, ByteBuf data) {
		try {

			switch (commandID) {
			case Protocol.MessageType.REQUEST_LOGIN_VALUE: {
				System.out.println("Handled login message, send response");

				Protocol.RequestLogin request = Protocol.RequestLogin.parseFrom(new ByteBufInputStream(data));
				if (HandleLoginRequest(request, channelHandlerContext)) {
					SetUser(new UserHandler());
				}
				break;
			}
			case Protocol.MessageType.REQUEST_REGISTER_VALUE: {
				System.out.println("Handled register message, send response");

				Protocol.RequestRegister request = Protocol.RequestRegister.parseFrom(new ByteBufInputStream(data));
				HandleRegisterRequest(request, channelHandlerContext);
				break;
			}
			default:
				System.out.println("Can't handle user message: " + commandID);
				break;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void SendResponseChannel(int cmdId, byte[] data, GeneratedMessage message, ChannelHandlerContext channel) {
		ByteBuf respBuf = channel.alloc().buffer();
		int size = data.length + 8;
		short flag = 0;

		respBuf.clear();
		respBuf.writeInt(size);
		respBuf.writeShort(flag);
		respBuf.writeShort(cmdId);
		respBuf.writeBytes(data);

		channel.writeAndFlush(respBuf).addListener(
				new MessageListener(null, cmdId, size, message != null ? message.getClass().getName() : "null", 0, 0));
	}

	public boolean HandleLoginRequest(Protocol.RequestLogin request, ChannelHandlerContext channelHandlerContext) {
		Protocol.ResponseLogin message = CouchBase.getInstance().handleRequest(request);
		SendResponseChannel(Protocol.MessageType.RESPONE_LOGIN_VALUE, message.toByteArray(), message,
				channelHandlerContext);

		return message.getResult() == ResponseCode.SUCCESS ? true : false;
	}

	public void HandleRegisterRequest(Protocol.RequestRegister request, ChannelHandlerContext channelHandlerContext) {
		GeneratedMessage message = CouchBase.getInstance().handleRequest(request);
		SendResponseChannel(Protocol.MessageType.RESPONE_REGISTER_VALUE, message.toByteArray(), message,
				channelHandlerContext);
	}
}
