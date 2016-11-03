/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.rpg.framework.client;

import io.netty.buffer.*;
import io.netty.channel.*;

public class SocketClientHandler extends ChannelInboundHandlerAdapter {
	private volatile Channel channel;
	private SocketClient socketClient;
	
	public SocketClientHandler(SocketClient client) {
		this.socketClient = client;
	}
	
	public boolean isRunning() {
		return true;
	}

	public SocketClientHandler() {
		
	}
	
	public void update() {
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) {
		channel = ctx.channel();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf data = (ByteBuf) msg;
		byte[] buffer = new byte[data.capacity() - 2];
		data.getBytes(2, buffer);
		receive(data.readShort(), buffer);		
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public void sendRequest(int commandID, byte[] data, boolean queue) {
		ByteBuf respBuf = channel.alloc().buffer();
		int size = data.length + 2;
		short flag = 0;

		respBuf.clear();
		respBuf.writeInt(size);
		respBuf.writeShort(flag);
		respBuf.writeShort(commandID);
		respBuf.writeBytes(data);

		channel.writeAndFlush(respBuf);
	}
	
	public void send(int commandID, byte[] data) {
		ByteBuf respBuf = channel.alloc().buffer();
		int size = data.length + 2;
		short flag = 0;

		respBuf.clear();
		respBuf.writeInt(size);
		respBuf.writeShort(flag);
		respBuf.writeShort(commandID);
		respBuf.writeBytes(data);

		channel.writeAndFlush(respBuf);
	}
	
	public void receive(int commandID, byte[] data) {
		socketClient.receive(commandID, data);
	}

	public SocketClient getSocketClient() {
		return socketClient;
	}

	public void setSocketClient(SocketClient socketClient) {
		this.socketClient = socketClient;
	}

}
