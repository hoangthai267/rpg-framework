package com.rpg.framework.data;

public class ChannelRequest {
	private int channelID;
	private int commandID;
	private byte[] data;

	public ChannelRequest(int channelID, int commandID, byte[] data) {
		this.channelID = channelID;
		this.commandID = commandID;
		this.data = data;
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
