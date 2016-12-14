package com.rpg.framework.test;

public class LocalClient {
	public static void main(String args[]) {
//		Client client = new Client();
		Client client = new Client(args[0], args[0]);
		if(client.initialize()) {
			client.start("127.0.0.1", 8463);
		}
	}
}
