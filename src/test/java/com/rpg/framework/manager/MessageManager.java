package com.rpg.framework.manager;

import java.util.ArrayList;
import java.util.List;

import com.rpg.framework.entity.Message;

public class MessageManager {
	private ArrayList<Message> 	messages;	
	private ArrayList<Message> 	updateMessages;
	private int 				messagesPerSecond;
	private int 				updateMessagesPerSecond;
	private boolean 			bGetting;
	
	public MessageManager() {
		messages 				= new ArrayList<Message>();
		updateMessages 			= new ArrayList<Message>();
		messagesPerSecond 		= 0;
		updateMessagesPerSecond	= 0;
		bGetting 				= false;
	}
	
	
	public void newMessage(Message message) {
		if(message == null)
			throw new NullPointerException();
		
		synchronized (messages) {
			messages.add(message);
		}
	}
	
	public void sendMessage(int channelID, int commandID, byte[] data) {
		newMessage(new Message(Message.SEND_TO_ONE, channelID, commandID, data));
	}
	
	public void sendMessage(List<Integer> channels, int commandID, byte[] data) {
		for (Integer channel : channels) {
			newMessage(new Message(Message.SEND_TO_ONE, channel, commandID, data));
		}
	}
	
	public void sendMessage(int commandID, byte[] data) {
		newMessage(new Message(Message.SEND_TO_ALL, commandID, data));
			
	}
	
	public void receiveMessage(int channelID, int commandID, byte[] data) {
		newMessage(new Message(Message.RECEIVE, channelID, commandID, data));
	}
	
	public ArrayList<Message> getMessages() {
		ArrayList<Message> list = null;
		
		bGetting 				= true;
		list 					= messages;		
		messagesPerSecond 		+= list.size();
	
		bGetting 				= false;
		messages 				= updateMessages;
		updateMessagesPerSecond += updateMessages.size();
		
		updateMessages 			= new ArrayList<Message>();
		
		return list;
	}
	
	public void print() {
		System.out.println("messagesPerSecond: " + messagesPerSecond + " updateMessagesPerSecond: " + updateMessagesPerSecond);
		messagesPerSecond 		= 0;
		updateMessagesPerSecond = 0;
	}
	
	public void update(double delta) {
		
	}
	
	private static MessageManager instance;
	
	public static MessageManager getInstance() {
		if (instance == null)
			instance = new MessageManager();
		return instance;
	}
}
