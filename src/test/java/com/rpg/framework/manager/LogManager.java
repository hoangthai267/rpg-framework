package com.rpg.framework.manager;

public class LogManager {
	private static boolean isEnable = true;
	
	public static void enable() {
		isEnable = true;
	}	
	
	public static void disable() {
		isEnable = false;
	}
	
	public static void print(Object s) {
		if(isEnable) {
			System.out.println(s);
		}
	}
}
