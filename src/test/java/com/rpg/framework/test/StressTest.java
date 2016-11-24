package com.rpg.framework.test;

import java.util.LinkedList;
import java.util.List;

import com.rpg.framework.core.Testing;

public class StressTest {

	public static void main(String args[]) {
		LinkedList<Client> list = new LinkedList<Client>();
		list.add(new Client());
		list.add(new Client());
		list.add(null);
		list.add(new Client());
		
		
		Client client = list.poll();
		while(!list.isEmpty()) {
			client = list.poll();
		}
		
		System.out.println(list.size());
		
	}
	
}
