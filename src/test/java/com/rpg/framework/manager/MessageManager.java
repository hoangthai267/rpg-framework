package com.rpg.framework.manager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.rpg.framework.entity.Message;

public class MessageManager {
	private ArrayList<Message> messages;	
	private ArrayList<Message> updateMessages;
	private int messagesPerSecond;
	private boolean bGetting;
	public MessageManager() {
		messages = new ArrayList<Message>();
		updateMessages = new ArrayList<Message>();
		messagesPerSecond = 0;
		bGetting = false;
	}
	
	public void newMessage(int type, int channelID, int commandID, byte[] data) {
		if (bGetting)
			updateMessages.add(new Message(type, channelID, commandID, data));
		else
			messages.add(new Message(type, channelID, commandID, data));
	}	
	
	public void newMessage(Message message) {
		if (bGetting)
			updateMessages.add(message);
		else
			messages.add(message);
	}
	
	public void sendMessage(int channelID, int commandID, byte[] data) {
		if (bGetting)
			updateMessages.add(new Message(Message.SEND_TO_ONE, channelID, commandID, data));
		else
			messages.add(new Message(Message.SEND_TO_ONE, channelID, commandID, data));
	}
	
	public void sendMessage(List<Integer> channels, int commandID, byte[] data) {
		for (Integer channel : channels) {
			if (bGetting)
				updateMessages.add(new Message(Message.SEND_TO_ONE, channel, commandID, data));
			else
				messages.add(new Message(Message.SEND_TO_ONE, channel, commandID, data));
		}
	}
	
	public void sendMessage(int commandID, byte[] data) {
		if (bGetting)
			updateMessages.add(new Message(Message.SEND_TO_ALL, commandID, data));
		else
			messages.add(new Message(Message.SEND_TO_ALL, commandID, data));
			
	}
	
	public void receiveMessage(int channelID, int commandID, byte[] data) {
		if (bGetting)
			updateMessages.add(new Message(Message.RECEIVE, channelID, commandID, data));
		else
			messages.add(new Message(Message.RECEIVE, channelID, commandID, data));
	}
	
	public List<Message> getMessages() {
		List<Message> list = new ArrayList<Message>();
		
		bGetting = true;
		
		while(!messages.isEmpty()) {
			Message message = messages.remove(0);
			if (message != null) {
				messagesPerSecond++;
				list.add(message);
			}
		}
		
		bGetting = false;
		
		return list;
	}
	
	public int getMessagesSize() {
		int result = messagesPerSecond;
		messagesPerSecond = 0;
		return result;
	}
	
	public void update(double delta) {
		while(!updateMessages.isEmpty()) {
			Message message = updateMessages.remove(0);
			if(message != null) {
				messages.add(message);
			}				
		}
	}
	
	private static MessageManager instance;
	
	public static MessageManager getInstance() {
		if (instance == null)
			instance = new MessageManager();
		return instance;
	}
}
