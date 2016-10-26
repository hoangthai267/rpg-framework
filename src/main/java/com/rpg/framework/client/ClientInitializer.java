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

import io.netty.channel.*;
import io.netty.channel.socket.*;
import io.netty.handler.ssl.*;
import io.netty.handler.timeout.*;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {
	public final static String PIPELINE_IDLE = "idle";
	public final static String PIPELINE_DECODER = "decoder";
	public final static String PIPELINE_HANDLER = "handler";
	
	/**
	 * In seconds
	 */
	public final static int IDLE_TIME_READER = 60 * 60; // second
	/**
	 * In seconds
	 */
	public final static int IDLE_TIME_WRITER = 60 * 60; // second
	/**
	 * In seconds
	 */
	public final static int IDLE_TIME_ALL = 60 * 60; // second
	
    private final SslContext sslCtx;

    public ClientInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc(), Client.HOST, Client.PORT));
        }
		p.addLast(PIPELINE_IDLE, new IdleStateHandler(IDLE_TIME_READER, IDLE_TIME_WRITER, IDLE_TIME_ALL));
		p.addLast(PIPELINE_DECODER, new ClientDecoder());
		p.addLast(PIPELINE_HANDLER, new ClientHandler());
    }
}
