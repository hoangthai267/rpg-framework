package com.rpg.framework.handler;

import java.io.IOException;

import com.couchbase.client.java.document.JsonDocument;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rpg.framework.data.*;
import com.rpg.framework.sever.*;
import com.rpg.framework.util.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;

public class GameServer {
	public static long startTime = 0l;
	
	public static Object HandleUserMessage(SocketServerHandler socketServerHandler, ChannelHandlerContext ctx, ByteBuf data) throws IOException {
		int cmdId = data.readShort();
		startTime = Time.currentTimeMillis();
		
		switch (cmdId) {
			case Protocol.MessageType.REQUEST_LOGIN_VALUE:		
			{
				System.out.println("Handled login message, send response");
				try {
					Protocol.RequestLogin request = Protocol.RequestLogin.parseFrom(new ByteBufInputStream(data));
					Protocol.ResponseLogin response = HandleLoginRequest(request, ctx);
					if(response.getResult() == Protocol.ResponseCode.SUCCESS)
						return response;
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}
			}
				break;
			case Protocol.MessageType.REQUEST_REGISTER_VALUE:
			{
				System.out.println("Handled register message, send response");
				try {
					Protocol.RequestRegister request = Protocol.RequestRegister.parseFrom(new ByteBufInputStream(data));
					HandleRegisterRequest(request, ctx);
				} catch (InvalidProtocolBufferException e) {
					e.printStackTrace();
				}
				break;
			}			
			default:
				System.out.println("Can't handle user message: " + cmdId);
				break;
		}
		
		return null;
	}
		
	public static GeneratedMessage SendResponseChannel(int cmdId
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
        
        channel.writeAndFlush(respBuf).addListener(new MessageListener(null,
        		cmdId,
        		size,
        		message != null ? message.getClass().getName() : "NullMsg",
        		startTime,
        		0
        ));
        return message;
	}
	
	public static Protocol.ResponseLogin HandleLoginRequest(Protocol.RequestLogin request, ChannelHandlerContext ctx) {	
		Protocol.ResponseLogin message = CouchBase.getInstance().handleRequest(request);
		SendResponseChannel(Protocol.MessageType.RESPONE_LOGIN_VALUE, message.toByteArray(), message, ctx);
		return message;
	}
	
	public static GeneratedMessage HandleRegisterRequest(Protocol.RequestRegister request, ChannelHandlerContext ctx) {
		GeneratedMessage message = CouchBase.getInstance().handleRequest(request);
		return SendResponseChannel(Protocol.MessageType.RESPONE_REGISTER_VALUE, message.toByteArray(), message, ctx);
	}
	
}
