package com.rpg.framework.entity;

import java.util.Random;

public class Monster {
	private int 	id;
	private int 	index;	
	
	private int 	mapID;
	private double 	positionX;
	private double 	positionY;
	
	private int		state;
	private int 	direction;
	
	private int		damage;
	private int		defense;
	private int		speed;
	
	private	int		curHP;
	private int		maxHP;
	private int		curMP;
	private int		maxMP;
	
	private double 	respawnTime;
	
	public Monster() {
		
		this.respawnTime = 5.0f;
	}
	
	public void update(double delta) {
		if(respawnTime > 0.0) {
			respawnTime -= delta;
		} else {
			//this.status.setCurHP(this.status.getCurHP() - 1);
		}		
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
	
	public int getMapID() {
		return mapID;
	}

	public void setMapID(int mapID) {
		this.mapID = mapID;
	}

	public double getPositionX() {
		return positionX;
	}

	public void setPositionX(double positionX) {
		this.positionX = positionX;
	}

	public double getPositionY() {
		return positionY;
	}

	public void setPositionY(double positionY) {
		this.positionY = positionY;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getDefense() {
		return defense;
	}

	public void setDefense(int defense) {
		this.defense = defense;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getCurHP() {
		return curHP;
	}

	public void setCurHP(int curHP) {
		this.curHP = curHP;
	}

	public int getMaxHP() {
		return maxHP;
	}

	public void setMaxHP(int maxHP) {
		this.maxHP = maxHP;
	}

	public int getCurMP() {
		return curMP;
	}

	public void setCurMP(int curMP) {
		this.curMP = curMP;
	}

	public int getMaxMP() {
		return maxMP;
	}

	public void setMaxMP(int maxMP) {
		this.maxMP = maxMP;
	}

	public double getRespawnTime() {
		return respawnTime;
	}

	public void setRespawnTime(double respawnTime) {
		this.respawnTime = respawnTime;
	}
	
	public Monster clone() {
		Monster entity = new Monster();
		
		entity.setId(id);
		entity.setIndex(index);
		
		entity.setMapID(mapID);
		entity.setPositionX(positionX);
		entity.setPositionY(positionY);
		
		entity.setState(state);
		entity.setDirection(direction);
		
		entity.setDamage(damage);
		entity.setDefense(defense);
		entity.setSpeed(speed);
		
		entity.setCurHP(curHP);
		entity.setMapID(maxHP);
		
		
		return entity;
	}
	
	public boolean isRespawn() {
		return respawnTime <= 0.0;
	}

	public boolean isDead() {
		return this.curHP <= 0;
	}
	
	public void setPosition(int mapID, double positionX, double positionY) {
		this.mapID = mapID;
		this.positionX = positionX;
		this.positionY = positionY;
	}

}
