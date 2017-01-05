package com.rpg.framework.entity;

import com.rpg.framework.database.Protocol;
import com.rpg.framework.manager.MessageManager;

public class User {
	private int	id;
	private int connectionID;
	private int occupation;
	private String name;
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
	
	private boolean	respawn;
	
	private int curEXP;
	private int maxEXP;
	private int level;
	
	public User() {
		id = -1;
		connectionID = -1;
		respawn = false;
	}
	
	public void update(double detla) {
		if(respawn) {
			respawn = false;
			
			curHP = maxHP;
			curMP = maxMP;
			positionX = 0.0;
			positionY = 0.0;
		
			MessageManager.getInstance().sendMessage(
					connectionID, 
					Protocol.MessageType.MESSAGE_RESPAWN_USER_VALUE, 
					Protocol.MessageRespawnUser.newBuilder()
					.setId(id)
					.setOccupation(occupation)
					.setName(name)
					
					.setMapID(mapID)
					.setX(positionX)
					.setY(positionY)
					
					.setMaxHP(maxHP)
					.setCurHP(curHP)
					.setMaxMP(maxMP)
					.setCurMP(curMP)
					
					.setDamage(damage)
					.setDefense(defense)
					.setSpeed(speed)
					.build().toByteArray());
		}

		if(curHP <= 0) {
			respawn = true;
			
			MessageManager.getInstance().sendMessage(
					connectionID, 
					Protocol.MessageType.MESSAGE_KILL_USER_VALUE, 
					Protocol.MessageKillUser.newBuilder().setId(id).build().toByteArray());
		}		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getConnectionID() {
		return connectionID;
	}

	public void setConnectionID(int connectionID) {
		this.connectionID = connectionID;
	}

	public int getOccupation() {
		return occupation;
	}

	public void setOccupation(int occupation) {
		this.occupation = occupation;
	}

	public int getMapID() {
		return mapID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public int getCurEXP() {
		return curEXP;
	}

	public void setCurEXP(int curEXP) {
		this.curEXP = curEXP;
	}

	public int getMaxEXP() {
		return maxEXP;
	}

	public void setMaxEXP(int maxEXP) {
		this.maxEXP = maxEXP;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setPosition(int mapID, double x, double y) {
		this.mapID = mapID;
		this.positionX = x;
		this.positionY = y;		
	}

	public void gainExp(int exp) {
		curEXP += exp;
		if(curEXP > maxEXP) {
			level += 1;
			
			curEXP = 0;
			maxEXP = maxEXP * 2;
			
			setMaxHP(maxHP + 10);
			setCurHP(maxHP);
			setMaxMP(maxMP + 10);
			setCurMP(maxMP);
			
			setDamage(damage + 10);
			setDefense(defense + 10);
			setSpeed(speed);
			
			MessageManager.getInstance().sendMessage(connectionID, Protocol.MessageType.MESSAGE_UP_LEVEL_USER_VALUE, Protocol.MessageUpLevelUser.newBuilder()
					.setLevel(level)
					.setCurEXP(curEXP)
					.setMaxEXP(maxEXP)
					.setCurHP(curHP)
					.setMaxHP(maxHP)
					.setCurMP(curMP)
					.setMaxMP(maxMP)
					.setDamage(damage)
					.setDefense(defense)
					.build().toByteArray());
		}
	}
}
