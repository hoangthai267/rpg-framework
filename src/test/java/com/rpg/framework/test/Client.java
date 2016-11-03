package com.rpg.framework.test;

import com.rpg.framework.client.SocketClient;

public class Client extends SocketClient {
	private String host;
	private int port;

	public Client(String host, int port) {
		super(host, port);
		this.host = host;
		this.port = port;
	}

	public void start() {
		byte[] data = new byte[10];
		data[1] = 10;
		data[2] = 25;
		send(1, data);
	}

	public void stop() {
	}

	public void update() {

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

	public void receive(int commandID, byte[] data) {
		send(1, data);
		System.out.println(data);
	}

	public static void main(String args[]) {
		new Client("localhost", 8364).start();
	}
}
