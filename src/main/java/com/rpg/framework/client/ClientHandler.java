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

import com.google.protobuf.GeneratedMessage;
import com.rpg.framework.data.Protocol;
import com.rpg.framework.data.Protocol.*;
import com.rpg.framework.sever.*;

public class ClientHandler extends ChannelInboundHandlerAdapter {
	// Stateful properties
	private volatile Channel channel;
	public ClientHandler() {

	}

	public GeneratedMessage sendMessage(int cmdID,
				byte[] data
            , GeneratedMessage message) {
		ByteBuf respBuf = channel.alloc().buffer();
        int size = data.length + 2;
        short flag = 0;

        respBuf.clear();
        respBuf.writeInt(size);
        respBuf.writeShort(flag);
        respBuf.writeShort(cmdID);
        respBuf.writeBytes(data);
        
        channel.writeAndFlush(respBuf);
        return message;
	}

	public String registerUser() {
		RequestRegister.Builder builder = RequestRegister.newBuilder();

		builder.setUsername("Admin");
		builder.setPassword("123456");
		RequestRegister message = builder.build();
		sendMessage(MessageType.REQUEST_REGISTER_VALUE, message.toByteArray(), message);
		return "";
	}

	public void writeAndFlush(byte[] data) {
		int len = data.length;
		channel.writeAndFlush(
				channel.alloc().buffer(len + SocketServerDecoder.HEADER_LEN).writeInt(len).writeBytes(data));
	}

	public void writeAndFlush(ByteBuf data) {
		int len = data.readableBytes();
		channel.writeAndFlush(
				channel.alloc().buffer(len + SocketServerDecoder.HEADER_LEN).writeInt(len).writeBytes(data));
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) {
		channel = ctx.channel();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// T viet chung vao day m tu refactor nhoe
		ByteBuf data = (ByteBuf) msg;
		int cmdID = data.readShort();
		switch (cmdID) {
		case MessageType.RESPONE_REGISTER_VALUE:
			System.out.println("WILL HANDLE LOGIN RESPONSE");
			Protocol.ResponseRegister response = Protocol.ResponseRegister.parseFrom(new ByteBufInputStream(data));
			System.out.println(response);
			break;

		default:
			break;
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
