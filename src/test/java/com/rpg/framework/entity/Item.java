package com.rpg.framework.entity;

public class Item {
	private int id;
	private int index;
	
	private int 	mapID;
	private double 	positionX;
	private double 	positionY;
	
	public Item() {
		this.id 		= -1;
		this.index 		= -1;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public Item clone() {
		Item result = new Item();
		
		result.index 	= index;
		result.id 		= id;
		result.mapID 	= mapID;
		result.positionX = positionX;
		result.positionY = positionY;
		
		return result;
	}
	
	
}
