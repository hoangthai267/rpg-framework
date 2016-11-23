package com.rpg.framework.test;

import java.util.LinkedList;
import java.util.List;

import com.rpg.framework.core.Testing;

public class StressTest {

	public static void main(String args[]) {
		List<com.rpg.framework.core.Client> clients = new LinkedList<com.rpg.framework.core.Client>();
		int from = Integer.parseInt(args[0]);
		int to = Integer.parseInt(args[1]);
		if(from >= to)
			return;
		to += 1;
		for(int i = from; i < to; i++) {
			Client client = new Client("admin" + i, "admin");
			if(client.initialize()) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new Testing(client, "128.199.255.44", 8463).start();
			}
		}
		
		
	}
	
}
