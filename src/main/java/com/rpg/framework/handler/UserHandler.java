package com.rpg.framework.handler;

import java.io.IOException;
import java.util.List;

import com.google.protobuf.*;
import com.rpg.framework.data.*;
import com.rpg.framework.sever.*;
import com.rpg.framework.util.*;

import io.netty.buffer.*;
import io.netty.channel.*;

public class UserHandler {

	public SocketServerHandler m_handlerSocket;
	public ChannelHandlerContext m_channel;
	public long startTime;
	private String userID;
	
	public void SendResponseChannel(int cmdId, byte[] data, GeneratedMessage message, ChannelHandlerContext channel) {
		ByteBuf respBuf = channel.alloc().buffer();
		int size = data.length + 8;
		short flag = 0;

		respBuf.clear();
		respBuf.writeInt(size);
		respBuf.writeShort(flag);
		respBuf.writeShort(cmdId);
		respBuf.writeBytes(data);

		channel.writeAndFlush(respBuf).addListener(new MessageListener(this, cmdId, size,
				message != null ? message.getClass().getName() : "NullMsg", startTime, 0));
	}

	public void handleRequest(int commandID, ByteBuf data) {
		startTime = Time.currentTimeMillis();
		try {
			switch (commandID) {
			case Protocol.MessageType.REQUEST_LIST_OF_CHARACTER_VALUE: {
				System.out.println("Handled list of character message, send response");

				Protocol.RequestListOfCharacter request = Protocol.RequestListOfCharacter
						.parseFrom(new ByteBufInputStream(data));
				handleRequestListOfCharacter(request);
				break;
			}
			case Protocol.MessageType.REQUEST_CREATE_CHARACTER_VALUE: {
				System.out.println("Handled create character message, send response");

				Protocol.RequestCreateCharacter request = Protocol.RequestCreateCharacter
						.parseFrom(new ByteBufInputStream(data));
				handleRequestCreateCharacter(request);
				break;
			}
			case Protocol.MessageType.REQUEST_START_GAME_VALUE: {
				System.out.println("Handled start game message, send response");

				Protocol.RequestStartGame request = Protocol.RequestStartGame.parseFrom(new ByteBufInputStream(data));
				handleRequestStartGame(request);
				break;
			}
			case Protocol.MessageType.REQUEST_UPDATE_POSITION_VALUE: {
				System.out.println("Handled udpate position message, send response");

				Protocol.RequestUpdatePosition request = Protocol.RequestUpdatePosition
						.parseFrom(new ByteBufInputStream(data));
				handleRequestUpdatePosition(request);
				break;
			}
			default:
				System.out.println("Can't handle message: " + commandID);
				break;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void handleRequestListOfCharacter(Protocol.RequestListOfCharacter request) {
		GeneratedMessage message = CouchBase.getInstance().handleRequest(request);
		SendResponseChannel(Protocol.MessageType.RESPONE_LIST_OF_CHARACTER_VALUE, message.toByteArray(), message,
				m_channel);
	}

	public void handleRequestCreateCharacter(Protocol.RequestCreateCharacter request) {
		GeneratedMessage message = CouchBase.getInstance().handleRequest(request);
		SendResponseChannel(Protocol.MessageType.RESPONE_CREATE_CHARACTER_VALUE, message.toByteArray(), message,
				m_channel);
	}

	public void handleRequestStartGame(Protocol.RequestStartGame request) {
		GeneratedMessage message = CouchBase.getInstance().handleRequest(request);
		SendResponseChannel(Protocol.MessageType.RESPONE_START_GAME_VALUE, message.toByteArray(), message, m_channel);
	}

	public void handleRequestUpdatePosition(Protocol.RequestUpdatePosition request) {
		GeneratedMessage message = CouchBase.getInstance().handleRequest(request);
		SendResponseChannel(Protocol.MessageType.RESPONE_UPDATE_POSITION_VALUE, message.toByteArray(), message,
				m_channel);
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

}
