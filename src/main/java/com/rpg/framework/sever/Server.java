package com.rpg.framework.sever;

public class Server {
	public static void main(String args[]) throws Exception {
		new SocketServer().start("localhost", 8463);
		System.out.println("Server start.");
	}
}
