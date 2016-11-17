package com.rpg.framework.sever;

import io.netty.channel.ChannelFuture;

public class ResultMessage implements io.netty.channel.ChannelFutureListener{
	private int channelID;
	private int commandID;
	
	public ResultMessage(int commandID, int channelID) {
		this.commandID = commandID; 
		this.channelID = channelID;
	}
	
	
	public void operationComplete(ChannelFuture future) throws Exception {
		// TODO Auto-generated method stub
//		System.out.println("Done channelID: " + channelID + " commandID: " + commandID) ;		
	}

}
