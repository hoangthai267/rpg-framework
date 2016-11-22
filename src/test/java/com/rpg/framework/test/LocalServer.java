package com.rpg.framework.test;

public class LocalServer {
	public static void main(String args[]) {
		Server server = new Server();
		if(server.initialize()) {
			server.start("127.0.0.1", 8463);
		}
		server.stop();
	}
}
