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

import java.io.IOException;
import java.util.*;

import com.rpg.framework.data.*;

public class ClientHandler extends ChannelInboundHandlerAdapter {
	private volatile Channel channel;

	private boolean running;
	private boolean flag;
	private long startTime;
	private long endTime;
	private Queue<Request> requestQueue;

	public boolean isRunning() {
		return running;
	}

	public ClientHandler() {
		this.running = true;
		this.flag = true;
		this.requestQueue = new LinkedList<Request>();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) {
		channel = ctx.channel();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf data = (ByteBuf) msg;
		handleResponse(data.readShort(), data);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public void sendRequest(int commandID, byte[] data, boolean queue) {
		if (queue) {
			requestQueue.add(new Request(commandID, data));
			return;
		}

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

	public void handleRequest() {
		if (flag) {
			if (requestQueue.size() == 0)
				return;
			startTime = System.currentTimeMillis();
			flag = false;
			Request request = requestQueue.poll();
			sendRequest(request.getCommandID(), request.getData(), false);

			System.out.println("Send request with command " + request.getCommandID());
		}
	}

	public void requestRegister(String name, String password) {
		Protocol.RequestRegister.Builder builder = Protocol.RequestRegister.newBuilder();
		builder.setUsername(name);
		builder.setPassword(password);

		sendRequest(Protocol.MessageType.REQUEST_REGISTER_VALUE, builder.build().toByteArray(), true);
	}

	public void requestLogin(String name, String password) {
		Protocol.RequestLogin.Builder builder = Protocol.RequestLogin.newBuilder();
		builder.setUsername(name);
		builder.setPassword(password);

		sendRequest(Protocol.MessageType.REQUEST_LOGIN_VALUE, builder.build().toByteArray(), true);
	}

	public void requestListOfCharacter(String userID) {
		Protocol.RequestListOfCharacter.Builder builder = Protocol.RequestListOfCharacter.newBuilder();
		builder.setUserID(userID);

		sendRequest(Protocol.MessageType.REQUEST_LIST_OF_CHARACTER_VALUE, builder.build().toByteArray(), true);
	}

	public void requestCreateCharacter(String userID, String name, String occupation) {
		Protocol.RequestCreateCharacter.Builder builder = Protocol.RequestCreateCharacter.newBuilder();
		builder.setUserID(userID);
		builder.setName(name);
		builder.setOccupation(name);

		sendRequest(Protocol.MessageType.REQUEST_CREATE_CHARACTER_VALUE, builder.build().toByteArray(), true);
	}

	public void requestStartGame(String userID, String charID) {
		Protocol.RequestStartGame.Builder builder = Protocol.RequestStartGame.newBuilder();
		builder.setUserID(userID);
		builder.setCharID(charID);

		sendRequest(Protocol.MessageType.REQUEST_START_GAME_VALUE, builder.build().toByteArray(), true);
	}

	public void requestUpdatePosition(String userID, String charID, String mapID, double x, double Y) {
		Protocol.RequestUpdatePosition.Builder builder = Protocol.RequestUpdatePosition.newBuilder();
		builder.setUserID(userID);
		builder.setCharID(charID);
		builder.setNewPosition(Protocol.CharacterPosition.newBuilder().setMapID(mapID).setX(x).setY(Y).build());

		sendRequest(Protocol.MessageType.REQUEST_UPDATE_POSITION_VALUE, builder.build().toByteArray(), false);
	}

	public void handleResponse(int commandID, ByteBuf data) {
		try {
			switch (commandID) {
			case Protocol.MessageType.RESPONE_REGISTER_VALUE: {
				Protocol.ResponseRegister response = Protocol.ResponseRegister.parseFrom(new ByteBufInputStream(data));
//				System.out.println(response.toString());
				break;
			}
			case Protocol.MessageType.RESPONE_LOGIN_VALUE: {
				Protocol.ResponseLogin response = Protocol.ResponseLogin.parseFrom(new ByteBufInputStream(data));
//				System.out.println(response.toString());
				break;
			}
			case Protocol.MessageType.RESPONE_LIST_OF_CHARACTER_VALUE: {
				Protocol.ResponseListOfCharacter response = Protocol.ResponseListOfCharacter
						.parseFrom(new ByteBufInputStream(data));
//				System.out.println(response);
				break;
			}
			case Protocol.MessageType.RESPONE_CREATE_CHARACTER_VALUE: {
				Protocol.ResponseCreateCharacter response = Protocol.ResponseCreateCharacter
						.parseFrom(new ByteBufInputStream(data));
//				System.out.println(response);
				break;
			}
			case Protocol.MessageType.RESPONE_START_GAME_VALUE: {
				Protocol.ResponseStartGame response = Protocol.ResponseStartGame
						.parseFrom(new ByteBufInputStream(data));
//				System.out.println(response);
				break;
			}
			case Protocol.MessageType.RESPONE_UPDATE_POSITION_VALUE: {
				Protocol.ResponseUpdatePosition response = Protocol.ResponseUpdatePosition
						.parseFrom(new ByteBufInputStream(data));
//				System.out.println(response);
				break;
			}
			default: {
				System.err.println("Can't handle reponse with command ID: " + commandID);
				break;
			}
			}
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		flag = true;
		endTime = System.currentTimeMillis();		
		System.out.println("Delay time: " + (endTime - startTime));
	}

	public void handleResponseListOfCharacter(Protocol.ResponseListOfCharacter response) {
	}

	public void handleResponseResponseCreateCharacter(Protocol.ResponseCreateCharacter response) {

	}

	public void handleResponseResponseStartGame(Protocol.ResponseStartGame response) {

	}

	public void handleResponseResponseUpdatePosition(Protocol.ResponseUpdatePosition response) {

	}
}
