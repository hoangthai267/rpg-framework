package com.rpg.framework.test;

public class CloudClient {
	public static void main(String args[]) {
//		new Client("128.199.255.44", 8463).start("admin", "admin");
		new Client("128.199.255.44", 8463).start(args[0], "admin");
	}
}
