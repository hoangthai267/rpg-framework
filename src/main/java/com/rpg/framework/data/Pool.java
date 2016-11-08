package com.rpg.framework.data;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import com.couchbase.client.java.document.json.JsonObject;

public class Pool {
	private CouchBase couchbase;
	private Spymemcached spymemcached;
	private Queue<String> data;
	private Timer pollTimer;
	
	public Pool(CouchBase couchbase, Spymemcached spymemcached) {
		this.couchbase = couchbase;
		this.spymemcached = spymemcached;
		this.data = new LinkedList<String>();
		this.pollTimer = new Timer();
		this.pollTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				update();
			}
		}, 0, 1000 / 1);
	}
	
	public void update() {
		for(int i = 0; i < 1000; i++) {
			if(data.size() == 0)
				break;
			String key = data.poll();
			JsonObject value = JsonObject.fromJson(spymemcached.get(key).toString());
			couchbase.set(key, value);
		}
	}
	
	public void set(String key, JsonObject value) {
		set(key, value, 6000);
	}
	
	public void set(String key, JsonObject value, int expTime) {
		if(!data.contains(key))
			data.add(key);
		spymemcached.set(key, value.toString(), expTime);
	}
	
	public String get(String key) {
		return spymemcached.get(key).toString();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
