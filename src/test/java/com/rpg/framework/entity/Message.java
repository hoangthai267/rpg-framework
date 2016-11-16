package com.rpg.framework.entity;

import java.util.List;

public class Message {
	public static int SEND_TO_ONE = 1;
	public static int SEND_TO_OTHER = 2;
	public static int SEND_TO_ALL = 3;
	public static int RECEIVE = 4;
	
	private int type;
	private int channelID;
	private List<Integer> channels;
	private int commandID;
	private byte[] data;
	
	
	public Message(int type, int channelID, int commandID, byte[] data) {
		this.type = type;
		this.channelID = channelID;
		this.commandID = commandID;
		this.data = data;
	}
	
	public Message(int type, List<Integer> channels, int commandID, byte[] data) {
		this.type = type;
		this.setChannels(channels);
		this.commandID = commandID;
		this.data = data;
	}


	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
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

	public List<Integer> getChannels() {
		return channels;
	}

	public void setChannels(List<Integer> channels) {
		this.channels = channels;
	}
	
}
