package com.rpg.framework.test;

public class CloudServer {
	public static void main(String args[]) {
		new Server("128.199.255.44", 8463).start();
//		new Server("128.199.255.44", 8888).start();
	}
}
