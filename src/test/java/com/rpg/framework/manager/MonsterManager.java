package com.rpg.framework.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

import com.rpg.framework.entity.Monster;
import com.rpg.framework.entity.Stats;
import com.rpg.framework.entity.Status;

public class MonsterManager {
	private HashMap<Integer, Monster> monstersPrototype;
	private HashMap<Integer, Monster> monstersList;
	private ArrayList<Monster> monsters;
	
	private LinkedList<Monster>	monstersRespawn;
	public MonsterManager() {
		monstersPrototype = new HashMap<Integer, Monster>();
		monstersList = new HashMap<Integer, Monster>();
		monstersRespawn = new LinkedList<Monster>();
		monsters = new ArrayList<Monster>();
		
		initialize();
	}
	
	public boolean initialize() {
		DataManager instance = DataManager.getInstance();
		JsonObject list = instance.getCouchbase().get("Prototype_Monsters");
		JsonArray data = list.getArray("data");
		int total = list.getInt("total");
		
		for(int i = 0; i < total; i++) {
			JsonObject object = data.getObject(i);
			
			Status status = new Status();
			status.setMaxHP(object.getInt("maxHP"));
			status.setMaxMP(object.getInt("maxMP"));
			status.setCurHP(status.getMaxHP());
			status.setCurMP(status.getMaxMP());
			
			Stats stats = new Stats();
			stats.setDamage(object.getInt("damage"));
			stats.setDefense(object.getInt("defense"));
			stats.setSpeed(object.getInt("speed"));
			

			Monster monster = new Monster();
			monster.setId(object.getInt("id"));
			monster.setStats(stats);
			monster.setStatus(status);
			
			monstersPrototype.put(monster.getId(), monster);
		}
		
		
		return true;
	}
	
	public void release() {
		
	}
	
	public void update(double delta) {
		for(int i = 0; i < monstersRespawn.size(); i++) {
			Monster entity = monstersRespawn.get(i);
			monstersList.put(entity.getIndex(), entity);
			monsters.add(entity);
		}
		monstersRespawn.clear();
		
		for (int i = 0; i < monsters.size(); i++)
		{
			Monster monster = monsters.get(i);
			monster.update(delta);
			if(monster.isDead()) {
				MapManager.getInstance().killMonster(monster.getPosition().getMapID(), monster.getIndex());
				MapManager.getInstance().respawnMonster(monster.getPosition().getMapID(), monster.getIndex());
				monstersList.remove(monster.getIndex(), monster);
				monsters.remove(i);
				i--;
			}
		}
	}
	
	public Monster getMonster(int id) {
		return monstersPrototype.get(id).clone();
	}
	
	public Monster getMonsterInList(int index) {
		return monstersList.get(index);
	}
	
	public void respawnMonster(Monster monster) {
		monstersRespawn.add(monster);
	}

	private static MonsterManager instance;

	public static MonsterManager getInstance() {
		if(instance == null)
			instance = new MonsterManager();
		return instance;
	}	
}
