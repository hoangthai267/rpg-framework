package com.rpg.framework.test;

public class LocalServer {
	public static void main(String args[]) {
		new Server("127.0.0.1", 8463).start();
	}
}
