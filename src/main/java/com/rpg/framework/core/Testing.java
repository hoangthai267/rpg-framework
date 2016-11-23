package com.rpg.framework.core;

public class Testing extends Thread {
	private Client client;
	private String host;
	private int port;
	
	public Testing(Client client, String host, int port) {
		this.client = client;
		this.host = host;
		this.port = port;
	}
	
	@Override
	public void run() {
		client.start(host, port);
	}
	
}
