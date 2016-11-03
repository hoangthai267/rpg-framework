package com.rpg.framework.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class SocketClientDecoder extends ByteToMessageDecoder {
	final static int HEADER_LEN = 4;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() <= HEADER_LEN) // 4 byte len, x byte data
		{
			return;
		}
		in.markReaderIndex();
		int bodyLen = in.readInt() - 6;
		@SuppressWarnings("unused")
		int flag = in.readShort();
		if (bodyLen <= 0) {
			ctx.close();
			return;
		}
		if (bodyLen > in.readableBytes()) {
			in.resetReaderIndex();
			return;
		}
		out.add(in.readBytes(bodyLen));
	}
}