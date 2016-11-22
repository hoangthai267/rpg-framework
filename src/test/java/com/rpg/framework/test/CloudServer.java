package com.rpg.framework.test;

public class CloudServer {
	public static void main(String args[]) {
		Server server = new Server();
		if(server.initialize()) {
			server.start("128.199.255.44", 8463);
		}
		server.stop();
		
	}
}
