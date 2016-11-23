package com.rpg.framework.manager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.rpg.framework.entity.Message;

public class MessageManager {
	private ArrayList<Message> messages;	
	private Queue<Message> updateMessages;	
	public MessageManager() {
		messages = new ArrayList<Message>();
		updateMessages = new LinkedList<Message>();
	}
	
	public void newMessage(int type, int channelID, int commandID, byte[] data) {
		updateMessages.add(new Message(type, channelID, commandID, data));
	}	
	
	public void newMessage(Message message) {
		updateMessages.add(message);
	}
	
	public void sendMessage(int channelID, int commandID, byte[] data) {
		updateMessages.add(new Message(Message.SEND_TO_ONE, channelID, commandID, data));
	}
	
	public void sendMessage(List<Integer> channels, int commandID, byte[] data) {
		for (Integer channel : channels) {
			updateMessages.add(new Message(Message.SEND_TO_ONE, channel, commandID, data));
		}
//		updateMessages.add(new Message(Message.SEND_TO_OTHER, channels, commandID, data));
	}
	
	public void sendMessage(int commandID, byte[] data) {
		updateMessages.add(new Message(Message.SEND_TO_ALL, commandID, data));
	}
	
	public void receiveMessage(int channelID, int commandID, byte[] data) {
		updateMessages.add(new Message(Message.RECEIVE, channelID, commandID, data));
	}
	
	public List<Message> getMessages() {
		messages.clear();
		
		Message message = updateMessages.poll();
		while(message != null) {
			messages.add(message);
			message = updateMessages.poll();
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
