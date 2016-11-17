package com.rpg.framework.manager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.rpg.framework.entity.Message;

public class MessageManager {
	private ArrayList<Message> messages;	
	private ArrayList<Message> updateMessages;	
	public MessageManager() {
		messages = new ArrayList<Message>();
		updateMessages = new ArrayList<Message>();
	}
	
	public void newMessage(int type, int channelID, int commandID, byte[] data) {
		updateMessages.add(new Message(type, channelID, commandID, data));
	}
	
	public void newMessage(int type, List<Integer> channels, int commandID, byte[] data) {
		updateMessages.add(new Message(type, channels, commandID, data));
	}
	
	public void newMessage(int type, int commandID, byte[] data) {
		updateMessages.add(new Message(type, -1, commandID, data));
	}	
	
	public List<Message> getMessages() {
		messages.clear();
		while(updateMessages.size() != 0) {			
			messages.add(updateMessages.remove(0));
		}
		return this.messages;
	}
	
	private static MessageManager instance;
	
	public static MessageManager getInstance() {
		if (instance == null)
			instance = new MessageManager();
		return instance;
	}
}
