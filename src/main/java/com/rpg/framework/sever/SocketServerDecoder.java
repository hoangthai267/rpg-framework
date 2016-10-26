package com.rpg.framework.sever;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class SocketServerDecoder extends ByteToMessageDecoder{
	final public static int HEADER_LEN = 6;
    final public static int MAX_CLIENT_PACKAGE_SIZE = 1024*64;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ///LogContext(ctx, "UserSocketDecoder.decode", in.readableBytes(), out.size());
		if (in.readableBytes() <= HEADER_LEN) //4 byte len, 2 byte flags, x byte data
            return;
        in.markReaderIndex();
        int bodyLen = in.readInt();
        @SuppressWarnings("unused")
		int flag = in.readShort();
		
        if (bodyLen <= 0 || bodyLen > MAX_CLIENT_PACKAGE_SIZE) 
        {
			System.out.println("Message is too big");
            ctx.close();
            return;
        }
		
        if (bodyLen > in.readableBytes()) 
		{
			in.resetReaderIndex();
            return;
        }
        out.add(in.readBytes(bodyLen));
        ///LogContext(ctx, "UserSocketDecoder.readDone", in.readableBytes(), out.size());
    }
}
