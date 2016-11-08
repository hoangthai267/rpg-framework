package com.rpg.framework.sever;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class SocketServerManager {
	List<ChannelHandlerContext> listChannel;
	Queue<ChannelHandlerContext> queueChannel;
	SocketServer server;

	public SocketServerManager(SocketServer server) {
		this.server = server;
		listChannel = new ArrayList<ChannelHandlerContext>();
		queueChannel = new LinkedList<ChannelHandlerContext>();
	}

	public void addChannel(ChannelHandlerContext ctx) {
		listChannel.add(ctx);
	}

	public void removeChannel(ChannelHandlerContext ctx) {
		listChannel.remove(ctx);
	}

	public void readChannel(ChannelHandlerContext ctx, int commandID, byte[] data) {
		queueChannel.add(ctx);
		server.handleMessage(commandID, data);
	}

	public void sendChannel(int type, int commandID, byte[] data) {
		switch (type) {
			case 0: {
				ChannelHandlerContext currentChannel = queueChannel.poll();
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
				ChannelHandlerContext currentChannel = queueChannel.poll();
				for (ChannelHandlerContext channel : listChannel) {
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
				queueChannel.poll();
				for (ChannelHandlerContext channel : listChannel) {
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
}
