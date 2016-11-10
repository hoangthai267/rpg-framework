package com.rpg.framework.sever;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import com.rpg.framework.data.ChannelRequest;
import com.rpg.framework.data.ChannelResponse;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class SocketServerManager {
	Map<Integer, ChannelHandlerContext> listChannel;
	Queue<ChannelRequest> channelRequests;
	Queue<ChannelResponse> channelResponses;
	SocketServer server;
	AtomicInteger index;
	
	public SocketServerManager(SocketServer server) {
		this.server = server;
		listChannel = new HashMap<Integer, ChannelHandlerContext>();
		channelRequests = new LinkedList<ChannelRequest>();
		channelResponses = new LinkedList<ChannelResponse>();
		
		this.index = new AtomicInteger(0);
	}

	public int addChannel(ChannelHandlerContext ctx) {
		listChannel.put(index.incrementAndGet(), ctx);
		return index.get();
	}

	public boolean removeChannel(int channelID) {
		return listChannel.remove(channelID) != null;
	}

	public void readChannel(int channelID, int commandID, byte[] data) {
//		channelRequests.add(new ChannelRequest(channelID, commandID, data));
		server.receive(channelID, commandID, data);
	}

	public void writeChannel(int channelID, int responseID, int commandID, byte[] data) {
		ChannelHandlerContext currentChannel = listChannel.get(channelID);
		switch (responseID) {
		case 0: {
			ByteBuf respBuf = currentChannel.alloc().buffer();
			int size = data.length + 8;
			short flag = 0;

			respBuf.clear();
			respBuf.writeInt(size);
			respBuf.writeShort(flag);
			respBuf.writeShort(commandID);
			respBuf.writeBytes(data);

			currentChannel.writeAndFlush(respBuf);
			break;
		}
		case 1: {
			for (ChannelHandlerContext channel : listChannel.values()) {
				if (channel.equals(currentChannel))
					continue;
				ByteBuf respBuf = channel.alloc().buffer();
				int size = data.length + 8;
				short flag = 0;

				respBuf.clear();
				respBuf.writeInt(size);
				respBuf.writeShort(flag);
				respBuf.writeShort(commandID);
				respBuf.writeBytes(data);

				channel.writeAndFlush(respBuf);
			}
			break;
		}
		case 2: {
			for (ChannelHandlerContext channel : listChannel.values()) {
				ByteBuf respBuf = channel.alloc().buffer();
				int size = data.length + 8;
				short flag = 0;

				respBuf.clear();
				respBuf.writeInt(size);
				respBuf.writeShort(flag);
				respBuf.writeShort(commandID);
				respBuf.writeBytes(data);

				channel.writeAndFlush(respBuf);
			}
			break;
		}
		default:
			break;
		}
	}
	
	public void sendChannel(int channelID , int responseID, int commandID, byte[] data) {
		ChannelHandlerContext currentChannel = listChannel.get(channelID);
		switch (responseID) {
			case 0: {
				ByteBuf respBuf = currentChannel.alloc().buffer();
				int size = data.length + 8;
				short flag = 0;
	
				respBuf.clear();
				respBuf.writeInt(size);
				respBuf.writeShort(flag);
				respBuf.writeShort(commandID);
				respBuf.writeBytes(data);
	
				currentChannel.writeAndFlush(respBuf);
				break;
			}
			case 1: {
				for (ChannelHandlerContext channel : listChannel.values()) {
					if (channel.equals(currentChannel))
						continue;
					ByteBuf respBuf = channel.alloc().buffer();
					int size = data.length + 8;
					short flag = 0;
	
					respBuf.clear();
					respBuf.writeInt(size);
					respBuf.writeShort(flag);
					respBuf.writeShort(commandID);
					respBuf.writeBytes(data);
	
					channel.writeAndFlush(respBuf);
				}
				break;
			}
			case 2: {
				for (ChannelHandlerContext channel : listChannel.values()) {
					ByteBuf respBuf = channel.alloc().buffer();
					int size = data.length + 8;
					short flag = 0;
	
					respBuf.clear();
					respBuf.writeInt(size);
					respBuf.writeShort(flag);
					respBuf.writeShort(commandID);
					respBuf.writeBytes(data);
	
					channel.writeAndFlush(respBuf);
				}
				break;
			}
		default:
			break;
		}
	}

	public void update(double delta) {
	}

	public Queue<ChannelRequest> getChannelRequests() {
		return channelRequests;
	}

}
