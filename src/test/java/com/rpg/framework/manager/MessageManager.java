package com.rpg.framework.manager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.rpg.framework.entity.Message;

public class MessageManager {
	private ArrayList<Message> 	messages;	
	private ArrayList<Message> 	updateMessages;
	private int 				messagesPerSecond;
	private int 				updateMessagesPerSecond;
	private boolean 			bGetting;
	private boolean				bReady;
	public MessageManager() {
		messages 				= new ArrayList<Message>();
		updateMessages 			= new ArrayList<Message>();
		messagesPerSecond 		= 0;
		updateMessagesPerSecond	= 0;
		bGetting 				= false;
		bReady					= true;
	}
	
	public void newMessage(int type, int channelID, int commandID, byte[] data) {

		newMessage(new Message(type, channelID, commandID, data));
//		if (bGetting)
//			updateMessages.add(new Message(type, channelID, commandID, data));
//		else
//			messages.add(new Message(type, channelID, commandID, data));
	}	
	
	public void newMessage(Message message) {
		if(message == null)
			throw new NullPointerException();
		
//		System.out.println("Begin");
		if(bReady) {
			bReady = false;
			bReady = messages.add(message);
		} else {
			newMessage(message);
		}
		
//		System.out.println("End");
//		if (bGetting)
//			updateMessages.add(message);
//		else
//			messages.add(message);
	}
	
	public void sendMessage(int channelID, int commandID, byte[] data) {

		newMessage(new Message(Message.SEND_TO_ONE, channelID, commandID, data));
//		if (bGetting)
//			updateMessages.add(new Message(Message.SEND_TO_ONE, channelID, commandID, data));
//		else
//			messages.add(new Message(Message.SEND_TO_ONE, channelID, commandID, data));
	}
	
	public void sendMessage(List<Integer> channels, int commandID, byte[] data) {
		for (Integer channel : channels) {

			newMessage(new Message(Message.SEND_TO_ONE, channel, commandID, data));
//			if (bGetting)
//				updateMessages.add(new Message(Message.SEND_TO_ONE, channel, commandID, data));
//			else
//				messages.add(new Message(Message.SEND_TO_ONE, channel, commandID, data));
		}
	}
	
	public void sendMessage(int commandID, byte[] data) {

		newMessage(new Message(Message.SEND_TO_ALL, commandID, data));
//		if (bGetting)
//			updateMessages.add(new Message(Message.SEND_TO_ALL, commandID, data));
//		else
//			messages.add(new Message(Message.SEND_TO_ALL, commandID, data));
			
	}
	
	public void receiveMessage(int channelID, int commandID, byte[] data) {

		newMessage(new Message(Message.RECEIVE, channelID, commandID, data));
//		if (bGetting)
//			updateMessages.add(new Message(Message.RECEIVE, channelID, commandID, data));
//		else
//			messages.add(new Message(Message.RECEIVE, channelID, commandID, data));
	}
	
	public ArrayList<Message> getMessages() {
		ArrayList<Message> list = null;
		
		bGetting = true;
		list = messages;
		int index = list.indexOf(null);
		if(index != -1)
			System.out.println(list);
		
		messagesPerSecond += list.size();

		bGetting = false;
//		messages = updateMessages;
//		if(messages.contains(null)) {
//			System.out.println("MessageManager.getMessages() 2");
//		}
		updateMessagesPerSecond += updateMessages.size();
		
		messages = new ArrayList<Message>();
		
		return list;
	}
	
	public void print() {
//		System.out.println("messagesPerSecond: " + messagesPerSecond + " updateMessagesPerSecond: " + updateMessagesPerSecond);
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
