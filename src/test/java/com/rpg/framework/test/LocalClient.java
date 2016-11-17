package com.rpg.framework.test;

public class LocalClient {
	public static void main(String args[]) {
//		new Client("127.0.0.1", 8463).start("admin", "admin");
		new Client("127.0.0.1", 8463).start(args[0], "admin");
	}
}
