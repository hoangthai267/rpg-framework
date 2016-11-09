package com.rpg.framework.data;

public class ChannelResponse {
	private int channelID;
	private int responseID;
	private int commandID;
	private byte[] data;

	public ChannelResponse(int channelID, int responseID, int commandID, byte[] data) {
		this.channelID = channelID;
		this.responseID = responseID;
		this.commandID = commandID;
		this.data = data;
	}

	public int getChannelID() {
		return channelID;
	}

	public void setChannelID(int channelID) {
		this.channelID = channelID;
	}

	public int getResponseID() {
		return responseID;
	}

	public void setResponseID(int responseID) {
		this.responseID = responseID;
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
