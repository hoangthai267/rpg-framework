package com.rpg.framework.sever;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import com.rpg.framework.handler.*;
import com.rpg.framework.util.*;

public class SocketServerHandler extends ChannelInboundHandlerAdapter {
	private static final AtomicInteger numConnection = new AtomicInteger();
	private static final String s_defaultRemoteAddress = "";
	private static final String s_defaultUserId = "";

	private UserHandler m_user;
	private ChannelHandlerContext m_ctx;

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
		this.m_ctx = ctx;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		numConnection.decrementAndGet();
		m_user = null;
		m_ctx = null;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf data = (ByteBuf) msg;

		if (m_user != null) {
			// handle user level command
			try {
				m_user.HandleMessage(data);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				if (GameServer.HandleUserMessage(this, ctx, data) != null) // handle
																			// successful
																			// .
				{
					SetUser(new UserHandler());
				}
				// else
				// {
				// CloseSocket(ctx);
				// System.out.println("Cannot handle message");
				// }
			} catch (Exception ex) {
				ex.printStackTrace();
			}
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
		ChannelPipeline pipeline = m_ctx.pipeline();
		if (pipeline.get(SocketServerInitializer.PIPELINE_IDLE) != null)
			pipeline.remove(SocketServerInitializer.PIPELINE_IDLE);
	}

	public void setIdleTime(int idleTimeReader, int idleTimeWriter, int idleTimeAll) {
		removeIdleTime();
		m_ctx.pipeline().addFirst(SocketServerInitializer.PIPELINE_IDLE,
				new IdleStateHandler(idleTimeReader, idleTimeWriter, idleTimeAll, TimeUnit.MILLISECONDS));
	}

	public void writeAndFlush(int cmd, int flag, byte[] data) {
		int len = data.length;
		ByteBuf sendBuf = m_ctx.alloc().buffer(len + SocketServerDecoder.HEADER_LEN);
		sendBuf.writeInt(len + 4).writeInt(cmd).writeShort(flag).writeBytes(data);
		m_ctx.writeAndFlush(sendBuf);
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
			m_user.m_channel = this.m_ctx;

			m_userId = AUser.getUserId();
			m_remoteAddress = this.m_ctx.channel().remoteAddress() != null
					? this.m_ctx.channel().remoteAddress().toString() : "contextNullAddress";
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
}
