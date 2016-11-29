package com.rpg.framework.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.rpg.framework.database.Protocol;
import com.rpg.framework.manager.MessageManager;
import com.rpg.framework.manager.MonsterManager;
import com.rpg.framework.manager.UserManager;

public class Map {
	private int id;
	private int width;
	private int height;
	
	private ArrayList<Integer> userList;
	
	private LinkedList<Integer> monstersList;
	private LinkedList<Integer> itemList;
	private LinkedList<Integer> portalList;
	
	private HashMap<Integer, Monster> monstersPrototype;
	private LinkedList<Monster> monstersRespawn;
//	private HashMap<Integer, Item> itemsPrototype;
//	private HashMap<Integer, Portal> portalsPrototype;
	
	public Map() {
		this.userList 		= new ArrayList<Integer>();
		
		this.monstersList 	= new LinkedList<Integer>();
		this.itemList 		= new LinkedList<Integer>();
		this.portalList 	= new LinkedList<Integer>();
		
		this.monstersPrototype = new HashMap<Integer, Monster>();
		this.monstersRespawn = new LinkedList<Monster>();
	}
	
	public boolean initalize() {
		Set<Integer> keySet = monstersPrototype.keySet();		
		for (Integer key : keySet) {
			monstersRespawn.add(monstersPrototype.get(key).clone());
		}
		
		return true; 
	}
	
	public void release() {
		
	}
	
	public void update(double delta) {		
		for(int i = 0; i < monstersRespawn.size(); i++) {
			Monster monster = monstersRespawn.get(i);
			monster.update(delta);
			if(monster.isRespawn()) {
				MessageManager.getInstance().sendMessage(
						UserManager.getInstance().getConnectionListFromIDList(userList), 
						Protocol.MessageType.MESSAGE_RESPAWN_MONSTER_VALUE, 
						Protocol.MessageRespawnMonster.newBuilder()
							.setMapID(this.id)
							.setMonsterIndex(monster.getIndex())
							.build()
							.toByteArray()
						);
				MonsterManager.getInstance().respawnMonster(monster);
				monstersList.add(monster.getIndex());
				monstersRespawn.remove(monster);
				i--;				
			}				
		}	
	}
	
	public void addUser(Integer userID) {		
		User newUser = UserManager.getInstance().getIdentifiedUser(userID);
		Protocol.MessageNewUser message = Protocol.MessageNewUser.newBuilder()
				.setUser(Protocol.User.newBuilder()
						.setId(newUser.getId())
						.setPosition(Protocol.Position.newBuilder()
								.setMapID(newUser.getPosition().getMapID())
								.setX(newUser.getPosition().getX())
								.setY(newUser.getPosition().getY()))
						.setStats(Protocol.Stats.newBuilder()
								.setDamage(newUser.getStats().getDamage())
								.setDefense(newUser.getStats().getDefense())
								.setSpeed(newUser.getStats().getSpeed()))
						.setStatus(Protocol.Status.newBuilder()
								.setCurHP(newUser.getStatus().getCurHP())
								.setCurMP(newUser.getStatus().getCurMP())
								.setMaxHP(newUser.getStatus().getMaxHP())
								.setMaxMP(newUser.getStatus().getMaxMP()))
						
						).build();
		
		for(int i = 0; i < userList.size(); i++) {
			int id = userList.get(i);
			int userConnectionID = UserManager.getInstance().getIdentifiedUser(id).getConnectionID();
			MessageManager.getInstance().sendMessage(userConnectionID, Protocol.MessageType.MESSAGE_NEW_USER_VALUE, message.toByteArray());
		}
		
		userList.add(userID);
	}
	
	public boolean removeUser(Integer userID) {
		userList.remove(userID);
		
		User oldUser = UserManager.getInstance().getIdentifiedUser(userID);
		Protocol.MessageNewUser message = Protocol.MessageNewUser.newBuilder()
				.setUser(Protocol.User.newBuilder()
						.setId(oldUser.getId())
						.setPosition(Protocol.Position.newBuilder()
								.setMapID(oldUser.getPosition().getMapID())
								.setX(oldUser.getPosition().getX())
								.setY(oldUser.getPosition().getY()))
						.setStats(Protocol.Stats.newBuilder()
								.setDamage(oldUser.getStats().getDamage())
								.setDefense(oldUser.getStats().getDefense())
								.setSpeed(oldUser.getStats().getSpeed()))
						.setStatus(Protocol.Status.newBuilder()
								.setCurHP(oldUser.getStatus().getCurHP())
								.setCurMP(oldUser.getStatus().getCurMP())
								.setMaxHP(oldUser.getStatus().getMaxHP())
								.setMaxMP(oldUser.getStatus().getMaxMP()))
						
						).build();
		
		for(int i = 0; i < userList.size(); i++) {
			int id = userList.get(i);
			int userConnectionID = UserManager.getInstance().getIdentifiedUser(id).getConnectionID();
			MessageManager.getInstance().sendMessage(userConnectionID, Protocol.MessageType.MESSAGE_DELETE_USER_VALUE, message.toByteArray());
		}
		
		return true;
	}
	
	public List<Integer> getUserList() {
		return userList;
	}
	
	public List<Integer> getMonsterList() {
		return monstersList;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public List<Integer> getItemList() {
		return itemList;
	}
	
	public void addMonstersPrototype(int index, Monster monster) {
		monstersPrototype.put(index, monster);
	}
	
	public void respawnMonster(int index) {
		monstersRespawn.add(monstersPrototype.get(index).clone());
	}

	public void killMonster(int monsterIndex) {
		monstersList.remove((Object)monsterIndex);
		MessageManager.getInstance().sendMessage(
				UserManager.getInstance().getConnectionListFromIDList(userList), 
				Protocol.MessageType.MESSAGE_KILL_MONSTER_VALUE, 
				Protocol.MessageKillMonster.newBuilder()
					.setMapID(this.id)
					.setMonsterIndex(monsterIndex)
					.build()
					.toByteArray()
				);
	}
}
