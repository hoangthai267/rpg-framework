package com.rpg.framework.test;

import com.rpg.framework.sever.SocketServer;

public class Server extends SocketServer {
	private String host;
	private int port;
	
	public Server(String host, int port) {
		super(host, port);
	}
	
	public static void main(String args[]) throws Exception {
		new Server("localhost", 8463).start();
		System.out.println("Server start.");
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public byte[] handleMessage(int commandID, byte[] data) {
		return data;		
	}
}
