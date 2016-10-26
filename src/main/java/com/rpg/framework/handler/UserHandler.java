package com.rpg.framework.handler;

import java.io.IOException;

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

	public void HandleMessage(ByteBuf data) throws IOException {
		int cmdId = data.readShort();
		startTime = Time.currentTimeMillis();
		switch (cmdId) {			
		case Protocol.MessageType.REQUEST_LIST_OF_CHARACTER_VALUE:
		{
			System.out.println("Handled list of character message, send response");
			try {
				Protocol.RequestListOfCharacter request = Protocol.RequestListOfCharacter.parseFrom(new ByteBufInputStream(data));
				HandleRequestListOfCharacter(request);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			break;
		}
		case Protocol.MessageType.REQUEST_CREATE_CHARACTER_VALUE:
		{
			System.out.println("Handled create character message, send response");				
			try {
				Protocol.RequestCreateCharacter request = Protocol.RequestCreateCharacter.parseFrom(new ByteBufInputStream(data));
				HandleRequestCreateCharacter(request);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			break;
		}
		case Protocol.MessageType.REQUEST_START_GAME_VALUE:
		{
			System.out.println("Handled start game message, send response");
			try {
				Protocol.RequestStartGame request = Protocol.RequestStartGame.parseFrom(new ByteBufInputStream(data));
				HandleRequestStartGame(request);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			break;
		}
		case Protocol.MessageType.REQUEST_UPDATE_POSITION_VALUE:
		{
			System.out.println("Handled udpate position message, send response");
			try {
				Protocol.RequestUpdatePosition request = Protocol.RequestUpdatePosition.parseFrom(new ByteBufInputStream(data));
				HandleRequestUpdatePosition(request);
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
			break;
		}
		default:
			System.out.println("Can't handle message: " + cmdId);
			break;
		}
	}

	public GeneratedMessage SendResponseChannel(int cmdId
            , byte[] data
            , GeneratedMessage message
            , ChannelHandlerContext channel) {
		ByteBuf respBuf = channel.alloc().buffer();
        int size = data.length + 8;
        short flag = 0;

        respBuf.clear();
        respBuf.writeInt(size);
        respBuf.writeShort(flag);
        respBuf.writeShort(cmdId);
        respBuf.writeBytes(data);
        
        channel.writeAndFlush(respBuf).addListener(new MessageListener(this, 
        		cmdId, 
        		size,
        		message != null ? message.getClass().getName() : "NullMsg",
        		startTime,
        		0
        ));
        return message;
	}
	
	public String getUserId() {

		return null;
	}

	public GeneratedMessage HandleRequestListOfCharacter(Protocol.RequestListOfCharacter request)	{		
		GeneratedMessage message = CouchBase.getInstance().handleRequest(request);
		return SendResponseChannel(Protocol.MessageType.RESPONE_LIST_OF_CHARACTER_VALUE, message.toByteArray(), message, m_channel);
	}	

	public GeneratedMessage HandleRequestCreateCharacter(Protocol.RequestCreateCharacter request)	{		
		GeneratedMessage message = CouchBase.getInstance().handleRequest(request);
		return SendResponseChannel(Protocol.MessageType.RESPONE_CREATE_CHARACTER_VALUE, message.toByteArray(), message, m_channel);
	}

	public GeneratedMessage HandleRequestStartGame(Protocol.RequestStartGame request)	{		
		GeneratedMessage message = CouchBase.getInstance().handleRequest(request);
		return SendResponseChannel(Protocol.MessageType.RESPONE_START_GAME_VALUE, message.toByteArray(), message, m_channel);
	}	

	public GeneratedMessage HandleRequestUpdatePosition(Protocol.RequestUpdatePosition request)	{		
		GeneratedMessage message = CouchBase.getInstance().handleRequest(request);
		return SendResponseChannel(Protocol.MessageType.RESPONE_UPDATE_POSITION_VALUE, message.toByteArray(), message, m_channel);
	}	

}
