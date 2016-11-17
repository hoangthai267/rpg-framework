package com.rpg.framework.client;

import io.netty.channel.ChannelFuture;

public class ResultMessage implements io.netty.channel.ChannelFutureListener{

	private int commandID;
	
	public ResultMessage(int commandID) {
		this.commandID = commandID; 
	}
	
	
	public void operationComplete(ChannelFuture future) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Done: " + commandID);		
	}

}
