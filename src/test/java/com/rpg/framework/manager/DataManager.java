package com.rpg.framework.manager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.rpg.framework.core.Database;


public class DataManager {
	
	private static final double CACHED_TIME 		= 2.0;
	
	private static final String	ACCOUNTS 			= "Accounts";
	private static final String PROTOTYPE_ITEMS 	= "Prototype_Items";
	private static final String PROTOTYPE_MAPS 		= "Prototype_Maps";
	private static final String PROTOTYPE_MONSTERS 	= "Prototype_Monsters";
	
	private Database 				database;
	
	private Queue<String> 			keys;
	private Map<String, JsonObject> data;
	private double 					cachedTime;
	
	private DataManager () {
		database 	= new Database("Static", "Dynamic");
		
		keys 		= new LinkedList<String>();
		cachedTime 	= 0.0;
		
		data		= new HashMap<String, JsonObject>();
	}

	public boolean initialize() {
		JsonObject accounts = database.getCouchbase(ACCOUNTS);
		data.put(ACCOUNTS, accounts);
		
		JsonObject prototypeItems = database.getCouchbase(PROTOTYPE_ITEMS);
		data.put(PROTOTYPE_ITEMS, prototypeItems);

		JsonObject prototypeMaps = database.getCouchbase(PROTOTYPE_MAPS);
		data.put(PROTOTYPE_ITEMS, prototypeMaps);
		
		JsonObject prototypeMonsters = database.getCouchbase(PROTOTYPE_MONSTERS);
		data.put(PROTOTYPE_ITEMS, prototypeMonsters);
				
		return true;
	}
	
	public JsonObject get(String key) {
		JsonObject value = data.get(key);
		if(value == null) {
			value = database.getCouchbase(key);
			data.put(key, value);
		}
		return value;
	}
	
	public void set(String key, JsonObject value) {
		database.setCouchbase(key, value);
	}

	public void update(double delta) {
		if (cachedTime > CACHED_TIME) {
			cachedTime -= CACHED_TIME;
			
			String key = keys.poll();
			
			while(key != null) {	
				JsonObject value = null;
				 try {
					 value = database.getMemcached(key);
				 } catch (Exception ex) {
					 value = data.get(key);
				 }				
				database.setCouchbase(key, value);
				key = keys.poll();
			}			
			
		} else {
			cachedTime += delta;
		}
	}
	
	public void cached(String key, JsonObject value) {
		if(!keys.contains(key))
			keys.add(key);
		data.put(key, value);
		database.setMemcached(key, value);
	}
	
	private static DataManager instance;
	
	public static DataManager getInstance() {
		if(instance == null)
			instance = new DataManager();
		return instance;
	}

	public static void setInstance(DataManager instance) {
		DataManager.instance = instance;
	}	
}
