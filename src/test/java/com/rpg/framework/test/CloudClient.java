package com.rpg.framework.test;

public class CloudClient {	
	public static void main(String args[]) {
		Client client = new Client(args[0], "admin");
//		Client client = new Client();
		if(client.initialize()) {
			client.start("128.199.255.44", 8463);
		}
	}
}
