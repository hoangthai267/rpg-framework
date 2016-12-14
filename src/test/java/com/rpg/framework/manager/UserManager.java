package com.rpg.framework.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.couchbase.client.java.document.json.JsonObject;
import com.rpg.framework.database.Protocol;
import com.rpg.framework.entity.User;

public class UserManager {
	private DataManager dataManager;
	private HashMap<Integer, User> userList;
	private HashMap<Integer, User> anonymousUsers;
	private HashMap<Integer, User> identifiedUsers;
	
	public UserManager() {
		userList = new HashMap<Integer, User>();
		anonymousUsers = new HashMap<Integer, User>();
		identifiedUsers = new HashMap<Integer, User>();
		dataManager = DataManager.getInstance();
	}
	
	public boolean initialize() {
		return true;
	}
	
	public void release() {
		
	}
	
	public void update(double delta) {
		
	}
	
	public void addUser(int id) {
		User user = new User();
		
		JsonObject userPosition = dataManager.get("User_" + id + "_Position");
		JsonObject userStats = dataManager.get("User_" + id + "_Stats");
		JsonObject userStatus = dataManager.get("User_" + id + "_Status");
		
		user.setMapID(userPosition.getInt("mapID"));
		user.setPositionX(userPosition.getDouble("x"));
		user.setPositionY(userPosition.getDouble("y"));
		
		user.setDamage(userStats.getInt("dame"));
		user.setDefense(userStats.getInt("defense"));
		user.setSpeed(userStats.getInt("speed"));
		
		user.setCurHP(userStatus.getInt("curHP"));
		user.setCurMP(userStatus.getInt("curMP"));
		user.setMaxHP(userStatus.getInt("maxHP"));
		user.setMaxMP(userStatus.getInt("maxMP"));
		
		addUser(id, user);
	}
	
	public void addUser(int id, User user) {
		userList.put(id, user);
	}
	
	public boolean removeUser(Integer id) {
		return userList.remove(id) != null;
	}
	
	public User getUser(int id) {
		return userList.get(id);
	}
	
	public void addAnonymousUser(int connectionID) {
		User anonymous = new User();
		anonymous.setConnectionID(connectionID);
		anonymousUsers.put(connectionID, anonymous);		
	}
	
	public void addIdentifiedUser(int connectionID, int userID) {
//		System.out.println("UserManager.addIdentifiedUser() connectionID:" + connectionID + " userID: " + userID);
		User identified = anonymousUsers.remove(connectionID);
		
		JsonObject userPosition = dataManager.get("User_" + userID + "_Position");
		JsonObject userStats = dataManager.get("User_" + userID + "_Stats");
		JsonObject userStatus = dataManager.get("User_" + userID + "_Status");
		
		identified.setMapID(userPosition.getInt("mapID"));
		identified.setPositionX(userPosition.getDouble("x"));
		identified.setPositionY(userPosition.getDouble("y"));
		
		identified.setName(userStats.getString("name"));
		identified.setOccupation(userStats.getInt("occupation"));
		identified.setDamage(userStats.getInt("dame"));
		identified.setDefense(userStats.getInt("defense"));
		identified.setSpeed(userStats.getInt("speed"));
		
		identified.setCurHP(userStatus.getInt("curHP"));
		identified.setCurMP(userStatus.getInt("curMP"));
		identified.setMaxHP(userStatus.getInt("maxHP"));
		identified.setMaxMP(userStatus.getInt("maxMP"));
		
		identified.setId(userID);
		
		identifiedUsers.put(userID, identified);
	}

	public User getAnonymousUser(int connectionID) {
		return anonymousUsers.get(connectionID);
	}
	
	public User getIdentifiedUser(int userID) {
		return identifiedUsers.get(userID);
	}
	
	public boolean removeAnonymousUser(int connectionID) {
		System.out.println("UserManager.removeAnonymousUser(): " + connectionID);
		return anonymousUsers.remove(connectionID) != null;
	}
	
	public boolean removeIdentifiedUser(int connectionID) {
		System.out.println("UserManager.removeIdentifiedUser(): " + connectionID);
		Iterator<User> iterator = identifiedUsers.values().iterator();
		while (iterator.hasNext()) {
			User user = iterator.next();
			if (connectionID == user.getConnectionID()) {
//				System.out.println("UserManager.removeIdentifiedUser() connectionID: " + connectionID + " userID: " + user.getId());
				JsonObject userObject = DataManager.getInstance().get("User_" + user.getId());
				userObject.put("hasLogin", false);
				DataManager.getInstance().set("User_" + user.getId(), userObject);
				MapManager.getInstance().exitMap(user.getId(), user.getMapID());				
				identifiedUsers.remove(user.getId());			
				return true;
			}
		}
		return false;
	}
	
	public List<Integer> getConnectionListFromIDList(List<Integer> idList) {
		List<Integer> connectionList = new ArrayList<Integer>();
		
		for (Integer id : idList) {
			connectionList.add(identifiedUsers.get(id).getConnectionID());
		}
		
		return connectionList;
	}
	
	private static UserManager instance;
	
	public static UserManager getInstance() {
		if(instance == null) {
			instance = new UserManager();
		}		
		return instance;
	}

	public void sendMessageUpdateMonsterByCommand(List<Integer> userList, byte[] data) {
		for (Integer id : userList) {
			User entity = identifiedUsers.get(id);
			MessageManager.getInstance().sendMessage(entity.getConnectionID(), Protocol.MessageType.MESSAGE_UPDATE_MONSTER_BY_COMMAND_VALUE, data);
		}	
	}
}
