package com.rpg.framework.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.rpg.framework.database.Protocol;
import com.rpg.framework.database.Protocol.MessageUpdateMonsterByCommand;
import com.rpg.framework.entity.Monster;

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
		JsonObject list = instance.get("Prototype_Monsters");
		JsonArray data = list.getArray("data");
		int total = list.getInt("total");
		
		for(int i = 0; i < total; i++) {
			JsonObject object = data.getObject(i);
			int id = object.getInt("id");
			
			int hp = object.getInt("maxHP");
			int mp = object.getInt("maxMP");
			
			int damage = object.getInt("damage");
			int defense = object.getInt("defense");
			int speed = object.getInt("speed");
			
			Monster entity = new Monster();
			entity.setId(id);
			entity.setCurHP(hp);
			entity.setMaxHP(hp);
			
			entity.setDamage(damage);
			entity.setDefense(defense);
			entity.setSpeed(speed);
			
			monstersPrototype.put(entity.getId(), entity);
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
				MapManager.getInstance().killMonster(monster.getMapID(), monster.getId(), monster.getIndex());
				MapManager.getInstance().respawnMonster(monster.getMapID(), monster.getId(), monster.getIndex());
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

	public void sendMessageUpdateMonsterByCommand(List<Integer> monstersList, List<Integer> userList) {
		Protocol.MessageUpdateMonsterByCommand.Builder builder = MessageUpdateMonsterByCommand.newBuilder();
		Random rad = new Random();
		for (Integer id : monstersList) {
			Monster entity = this.monstersList.get(id.intValue());
			if(entity == null)
				continue;
			int command = Math.abs(rad.nextInt() % 3);
			
			builder.addData(Protocol.ActionCommand.newBuilder()
					.setCommand(command)
					.setID(entity.getId())
					.setIndex(entity.getIndex())
					);			
		}
		
		UserManager.getInstance().sendMessageUpdateMonsterByCommand(userList, builder.build().toByteArray());
	}	
}
