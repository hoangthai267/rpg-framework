package com.rpg.framework.manager;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.rpg.framework.entity.Message;

public class MessageManager {
	private LinkedList<Message> messages;	
		
	public MessageManager() {
		messages = new LinkedList<Message>();
	}
	
	public void newMessage(int type, int channelID, int commandID, byte[] data) {
		messages.add(new Message(type, channelID, commandID, data));
	}
	
	public void newMessage(int type, List<Integer> channels, int commandID, byte[] data) {
		messages.add(new Message(type, channels, commandID, data));
	}
	
	public void newMessage(int type, int commandID, byte[] data) {
		messages.add(new Message(type, -1, commandID, data));
	}	
	
	public Queue<Message> getMessages() {
		return this.messages;
	}
	
	private static MessageManager instance;
	
	public static MessageManager getInstance() {
		if (instance == null)
			instance = new MessageManager();
		return instance;
	}
}
