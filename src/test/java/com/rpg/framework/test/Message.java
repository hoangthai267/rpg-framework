package com.rpg.framework.test;

public class Message {
	private int channelID;
	private int commandID;
	private byte[] data;
	
	public Message(int channelID, int commandID, byte[] data) {
		this.setChannelID(channelID);
		this.setCommandID(commandID);
		this.setData(data);
	}

	public int getChannelID() {
		return channelID;
	}

	public void setChannelID(int channelID) {
		this.channelID = channelID;
	}

	public int getCommandID() {
		return commandID;
	}

	public void setCommandID(int commandID) {
		this.commandID = commandID;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
}
