package com.rpg.framework.manager;

import java.util.LinkedList;
import java.util.Queue;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.rpg.framework.core.Database;


public class DataManager {
	private static final double CACHED_TIME = 2.0;
	
	private Queue<String> 	keys;
	private double 			cachedTime;
	
	private Database 		database;
	
	private DataManager () {
		keys 		= new LinkedList<String>();
		cachedTime 	= 0.0;
		
		database 	= new Database("Static", "Dynamic");
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
	
	public JsonObject get(String key) {
		return database.getCouchbase(key);
	}
	
	public void set(String key, JsonObject value) {
		database.setCouchbase(key, value);
	}

	public void update(double delta) {
		if (cachedTime > CACHED_TIME) {
			cachedTime -= CACHED_TIME;
			
			String key = keys.poll();
			
			while(key != null) {
				database.setCouchbase(key, database.getMemcached(key));
				key = keys.poll();
			}			
			
		} else {
			cachedTime += delta;
		}
	}
	
	public void cached(String key, JsonObject value) {
		if(!keys.contains(key))
			keys.add(key);
		database.setMemcached(key, value);
	}
	
}
