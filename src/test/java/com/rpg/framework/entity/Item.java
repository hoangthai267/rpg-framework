package com.rpg.framework.entity;

public class Item {
	private int id;
	private int index;
	private Position position;
	
	public Item() {
		this.id 		= -1;
		this.index 		= -1;
		this.position 	= new Position();
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

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}
	
	public Item clone() {
		Item result = new Item();
		
		result.index 	= index;
		result.id 		= id;
		result.position	= new Position();
		result.position.set(position.getMapID(), position.getX(), position.getY());
		
		return result;
	}
	
	
}
