package com.rpg.framework.entity;

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
	
	private LinkedList<Integer> userList;
	
	private LinkedList<Integer> monstersList;
	private LinkedList<Integer> itemList;
	private LinkedList<Integer> portalList;
	
	private HashMap<Integer, MapObject> monsters;
	private HashMap<Integer, MapObject> items;
	private HashMap<Integer, MapObject> portals;
	
	
	private HashMap<Integer, Monster> monstersPrototype;
	private LinkedList<Monster> monstersRespawn;
//	private HashMap<Integer, Item> itemsPrototype;
//	private HashMap<Integer, Portal> portalsPrototype;
	
	public Map() {
		this.userList 		= new LinkedList<Integer>();
		
		this.monstersList 	= new LinkedList<Integer>();
		this.itemList 		= new LinkedList<Integer>();
		this.portalList 	= new LinkedList<Integer>();
		
		this.monsters 		= new HashMap<Integer, MapObject>();
		this.items 			= new HashMap<Integer, MapObject>();
		this.portals 		= new HashMap<Integer, MapObject>();
		
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
				MessageManager.getInstance().newMessage(
						Message.SEND_TO_OTHER, 
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
		userList.add(userID);
	}
	
	public boolean removeUser(Integer userID) {
		return userList.remove(userID);
	}
	
	public List<Integer> getUserList() {
		return userList;
	}
	
	public List<Integer> getMonsterList() {
		return monstersList;
	}
	
	public void addMonster(Integer id, MapObject monster) {
		monsters.put(id, monster);
	}
	
	public void addItem(Integer id, MapObject item) {
		items.put(id, item);
	}
	
	public void addPortal(Integer id, MapObject portal) {
		portals.put(id, portal);
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
		MessageManager.getInstance().newMessage(
				Message.SEND_TO_OTHER, 
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
