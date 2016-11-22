package com.rpg.framework.test;

public class CloudClient {	
	public static void main(String args[]) {
//		GameClient client = new GameClient(args[0], "admin");
		Client client = new Client();
		if(client.initialize()) {
			client.start("128.199.255.44", 8463);
		}
	}
}
