package com.rpg.framework.entity;

import java.util.HashMap;
import java.util.Map.Entry;

import com.rpg.framework.manager.MessageManager;

import java.util.Random;
import java.util.Set;

public class Monster {
	private int id;
	private int index;

	private int mapID;
	private double positionX;
	private double positionY;

	private int state;
	private int direction;

	private int damage;
	private int defense;
	private int speed;

	private int curHP;
	private int maxHP;
	private int curMP;
	private int maxMP;
	
	private int exp;

	private HashMap<Integer, Integer> attackList;

	private double respawnTime;

	public Monster() {
		this.exp = 100;
		this.respawnTime = 5.0f;
		this.attackList = new HashMap<Integer, Integer>();
	}

	public void update(double delta) {
		if (respawnTime > 0.0) {
			respawnTime -= delta;
		} else {
			// this.status.setCurHP(this.status.getCurHP() - 1);
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
		entity.setMaxHP(maxHP);

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

	public void attacked(int userID, int damage) {
		if (curHP < 0)
			return;

		if (curHP - damage > 0) {
			if (attackList.containsKey(userID)) {
				attackList.put(userID, attackList.get(userID) + damage);
			} else {
				attackList.put(userID, damage);
			}
		} else {
			if (attackList.containsKey(userID)) {
				attackList.put(userID, attackList.get(userID) + curHP);
			} else {
				attackList.put(userID, damage);
			}
		}
		
		curHP -= damage;
	}
	
	public int getExp(int userID) {
		if(!attackList.containsKey(userID))
			return 0;
		
		return attackList.get(userID).intValue() * exp / maxHP;
	}

}
