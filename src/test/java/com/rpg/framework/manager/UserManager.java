package com.rpg.framework.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.couchbase.client.java.document.json.JsonObject;
import com.rpg.framework.entity.Position;
import com.rpg.framework.entity.Stats;
import com.rpg.framework.entity.Status;
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
		
		Position position = new Position();
		position.setMapID(userPosition.getInt("mapID"));
		position.setX(userPosition.getDouble("x"));
		position.setY(userPosition.getDouble("y"));
		
		Stats stats = new Stats();
		stats.setDamage(userStats.getInt("dame"));
		stats.setDefense(userStats.getInt("defense"));
		stats.setSpeed(userStats.getInt("speed"));
		
		Status status = new Status();
		status.setCurHP(userStatus.getInt("curHP"));
		status.setCurMP(userStatus.getInt("curMP"));
		status.setMaxHP(userStatus.getInt("maxHP"));
		status.setMaxMP(userStatus.getInt("maxMP"));
		
		user.setPosition(position);
		user.setStats(stats);
		user.setStatus(status);
		
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
		
		Position position = new Position();
		position.setMapID(userPosition.getInt("mapID"));
		position.setX(userPosition.getDouble("x"));
		position.setY(userPosition.getDouble("y"));
		
		Stats stats = new Stats();
		stats.setDamage(userStats.getInt("dame"));
		stats.setDefense(userStats.getInt("defense"));
		stats.setSpeed(userStats.getInt("speed"));
		
		Status status = new Status();
		status.setCurHP(userStatus.getInt("curHP"));
		status.setCurMP(userStatus.getInt("curMP"));
		status.setMaxHP(userStatus.getInt("maxHP"));
		status.setMaxMP(userStatus.getInt("maxMP"));
		
		identified.setId(userID);
		identified.setPosition(position);
		identified.setStats(stats);
		identified.setStatus(status);
		
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
				MapManager.getInstance().exitMap(user.getId(), user.getPosition().getMapID());				
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
}
