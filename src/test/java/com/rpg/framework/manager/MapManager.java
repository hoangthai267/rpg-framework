package com.rpg.framework.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.rpg.framework.entity.Map;
import com.rpg.framework.entity.MapObject;
import com.rpg.framework.entity.Monster;
import com.rpg.framework.entity.Position;

public class MapManager {
	HashMap<Integer, Map>	mapList;
	
	public MapManager() {
		mapList = new HashMap<Integer, Map>();
		
		initialize();
	}
	
	public boolean initialize() {
		DataManager instance = DataManager.getInstance();
		MonsterManager monsterManager = MonsterManager.getInstance();
		
		JsonObject maps = instance.getCouchbase().get("Prototype_Maps");
		JsonArray normalMaps = maps.getArray("normalMaps");
		
		for (int i = 0; i < normalMaps.size(); i++) {
			Map map = new Map();
			
			JsonObject object = normalMaps.getObject(i);
			
			map.setId(object.getInt("id"));
			map.setWidth(object.getInt("width"));
			map.setHeight(object.getInt("height"));
			
			JsonArray monsters = object.getArray("monsters");
			for(int j = 0; j < monsters.size(); j++) {
				JsonObject monster = monsters.getObject(j);
				int monsterID = monster.getInt("id");
				int monsterIndex = monster.getInt("index");
				int monsterPositionX = monster.getInt("x");
				int monsterPositionY = monster.getInt("y");
				
				Position position = new Position();			
				position.setMapID(map.getId());
				position.setX(monsterPositionX);
				position.setY(monsterPositionY);
				
				Monster entity = monsterManager.getMonster(monsterID);
				entity.setId(monsterID);
				entity.setIndex(monsterIndex);
				entity.setPosition(position);
				
				map.addMonstersPrototype(monsterIndex, entity);
			}
			
			JsonArray items = object.getArray("items");
			for(int j = 0; j < items.size(); j++) {
				JsonObject item = items.getObject(j);
				int itemID = item.getInt("id");
				int itemPositionX = item.getInt("x");
				int itemPositionY = item.getInt("y");
				
				map.addItem(itemID, new MapObject(itemID, itemPositionX, itemPositionY));
			}
			
			JsonArray portals = object.getArray("portals");
			for(int j = 0; j < portals.size(); j++) {
				JsonObject portal = portals.getObject(j);
				int portalID = portal.getInt("id");
				int portalPositionX = portal.getInt("x");
				int portalPositionY = portal.getInt("y");
				
				map.addPortal(portalID, new MapObject(portalID, portalPositionX, portalPositionY));
			}
			
			map.initalize();
			
			mapList.put(map.getId(), map);
		}
		
		return true;
	}
	
	public void release() {
		
	}
	
	public void update(double delta) {
		Set<Integer> keySet = mapList.keySet();
		for (Integer key : keySet) {
			mapList.get(key).update(delta);
		}
	}
	
	public void changeMap(int userID, int from, int to) {
		mapList.get(from).removeUser(userID);
		mapList.get(to).addUser(userID);
	}
	
	public void enterMap(int userID, int mapID) {
		mapList.get(mapID).addUser(userID);
	}
	
	public List<Integer> getUserList(int mapID) {
		return mapList.get(mapID).getUserList();
	}
	
	public static void main(String args[]) {
		new MapManager().initialize();
	}

	public List<Integer> getMonsterList(int mapID) {
		return mapList.get(mapID).getMonsterList();
	}
	
	public List<Integer> getItemList(int mapID) {
		return mapList.get(mapID).getItemList();
	}
	
	public void respawnMonster(int mapID, int monsterIndex) {
		mapList.get(mapID).respawnMonster(monsterIndex);
	}
	
	public void killMonster(int mapID, int monsterIndex) {
		mapList.get(mapID).killMonster(monsterIndex);
	}
	
	private static MapManager instance;
	
	public static MapManager getInstance() {
		if(instance == null) {
			instance = new MapManager();
		}
		return instance;
	}
	
}
