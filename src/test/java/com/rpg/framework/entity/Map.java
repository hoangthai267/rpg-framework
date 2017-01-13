package com.rpg.framework.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.rpg.framework.database.Protocol;
import com.rpg.framework.manager.DataManager;
import com.rpg.framework.manager.MessageManager;
import com.rpg.framework.manager.MonsterManager;
import com.rpg.framework.manager.UserManager;

public class Map {
	private static double REFRESH_TIME = 2.0;
	
	
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
//	private LinkedList<Monster> itemsRespawn;
//	private HashMap<Integer, Portal> portalsPrototype;
	
	private int updatedUser;
	
	private double refreshTime;
	
	public Map() {
		this.userList 		= new ArrayList<Integer>();
		
		this.monstersList 	= new LinkedList<Integer>();
		this.itemList 		= new LinkedList<Integer>();
		this.portalList 	= new LinkedList<Integer>();
		
		this.monstersPrototype = new HashMap<Integer, Monster>();
		this.monstersRespawn = new LinkedList<Monster>();
		
		this.updatedUser	= -1;
		
		this.refreshTime = REFRESH_TIME;
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
							.setX(monster.getPositionX())
							.setY(monster.getPositionY())
							.setMonsterID(monster.getId())
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
		
		if(refreshTime > REFRESH_TIME) {
			refreshTime -= REFRESH_TIME;
			MonsterManager.getInstance().sendMessageUpdateMonsterByCommand(monstersList, userList);
		} else {
			refreshTime += delta;
		}
	}
	
	public void addUser(Integer userID) {		
		User newUser = UserManager.getInstance().getIdentifiedUser(userID);
		Protocol.MessageNewUser message = Protocol.MessageNewUser.newBuilder()
				.setId(newUser.getId())
				.setOccupation(newUser.getOccupation())
				.setName(newUser.getName())
				
				.setMapID(newUser.getMapID())
				.setX(newUser.getPositionX())
				.setY(newUser.getPositionY())
				
				.setDamage(newUser.getDamage())
				.setDefense(newUser.getDefense())
				.setSpeed(newUser.getSpeed())
				
				.setCurHP(newUser.getCurHP())
				.setCurMP(newUser.getCurMP())
				.setMaxHP(newUser.getMaxHP())
				.setMaxMP(newUser.getMaxMP())
			
				.build();
		
		for(int i = 0; i < userList.size(); i++) {
			int id = userList.get(i);
			int userConnectionID = UserManager.getInstance().getIdentifiedUser(id).getConnectionID();
			MessageManager.getInstance().sendMessage(userConnectionID, Protocol.MessageType.MESSAGE_NEW_USER_VALUE, message.toByteArray());
		}
		
		userList.add(userID);
		
		checkUpdatedUser(userID, true);
	}
	
	public boolean removeUser(Integer userID) {
		userList.remove(userID);
		
		User oldUser = UserManager.getInstance().getIdentifiedUser(userID);
		Protocol.MessageDeleteUser message = Protocol.MessageDeleteUser.newBuilder()
						.setId(oldUser.getId())								
						.build();
		
		for(int i = 0; i < userList.size(); i++) {
			int id = userList.get(i);
			int userConnectionID = UserManager.getInstance().getIdentifiedUser(id).getConnectionID();
			MessageManager.getInstance().sendMessage(userConnectionID, Protocol.MessageType.MESSAGE_DELETE_USER_VALUE, message.toByteArray());
		}
		
		checkUpdatedUser(userID, false);
		
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
	
	public void respawnMonster(int monsterID, int monsterIndex) {
		monstersRespawn.add(monstersPrototype.get(monsterIndex).clone());
	}

	public void killMonster(int monsterID, int monsterIndex) {
		monstersList.remove((Object)monsterIndex);
		MessageManager.getInstance().sendMessage(
				UserManager.getInstance().getConnectionListFromIDList(userList), 
				Protocol.MessageType.MESSAGE_KILL_MONSTER_VALUE, 
				Protocol.MessageKillMonster.newBuilder()
					.setMapID(this.id)
					.setMonsterID(monsterID)
					.setMonsterIndex(monsterIndex)
					.build()
					.toByteArray()
				);
	}
	
	public void killMonster(Monster monster) {
		monstersList.remove((Integer)monster.getIndex());
		for (Integer id : userList) {
			User user = UserManager.getInstance().getIdentifiedUser(id.intValue());
			int exp = monster.getExp(id.intValue());
			MessageManager.getInstance().sendMessage(
					user.getConnectionID(), 
					Protocol.MessageType.MESSAGE_KILL_MONSTER_VALUE, 
					Protocol.MessageKillMonster.newBuilder()
						.setMapID(this.id)
						.setMonsterID(monster.getId())
						.setMonsterIndex(monster.getIndex())
						.setBonusExp(exp)
						.setBonusMoney(monster.getMoney())
						.setBonusMoneyID(DataManager.getInstance().generateMoneyID())
						.build()
						.toByteArray()
					);
			
			user.gainExp(exp);
		}
	}

	public void sendMessageUpdateMonsterState(byte[] byteArray) {
		for (Integer integer : userList) {
			MessageManager.getInstance().sendMessage(
					UserManager.getInstance().getIdentifiedUser(integer.intValue()).getConnectionID(), 
					Protocol.MessageType.MESSAGE_UPDATE_MONSTER_STATE_VALUE, 
					byteArray);
		}
	}
	
	public boolean getUpdatedUser(int userID) {
		if(userList.get(0).intValue() == userID)
			return true;
		return false;
	}
	
	public void checkUpdatedUser(int userID, boolean flag) {
		//enter map
		if(flag) {
			if(updatedUser == -1) {
				updatedUser = userID;
				MessageManager.getInstance().sendMessage(UserManager.getInstance().getIdentifiedUser(updatedUser).getConnectionID(),
						Protocol.MessageType.MESSAGE_REQUEST_USER_UPDATE_MAP_INFORMATION_VALUE,
						Protocol.MessageRequestUserUpdateMapInformation.newBuilder().setMapID(id).build().toByteArray());
			}	
		}
		// exit map
		else {
			if(updatedUser == userID) {
				if(userList.size() > 0) {
					updatedUser = userList.get(0);
					MessageManager.getInstance().sendMessage(UserManager.getInstance().getIdentifiedUser(updatedUser).getConnectionID(),
							Protocol.MessageType.MESSAGE_REQUEST_USER_UPDATE_MAP_INFORMATION_VALUE,
							Protocol.MessageRequestUserUpdateMapInformation.newBuilder().setMapID(id).build().toByteArray());
				} else {
					updatedUser = -1;
				}
			}
		}
		
	}
}
