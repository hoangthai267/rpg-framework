package com.rpg.framework.manager;

import com.rpg.framework.data.CouchBase;
import com.rpg.framework.data.Pool;
import com.rpg.framework.data.Spymemcached;

public class DataManager {
	private CouchBase couchbase;
	private Spymemcached spymemcached;
	private Pool pool;
	
	private DataManager () {
//		couchbase = new CouchBase("Static");
//		spymemcached = new Spymemcached("Dynamic", "");
//		pool = new Pool(couchbase, spymemcached);
	}

	public CouchBase getCouchbase() {
		return couchbase;
	}

	public void setCouchbase(CouchBase couchbase) {
		this.couchbase = couchbase;
	}

	public Spymemcached getSpymemcached() {
		return spymemcached;
	}

	public void setSpymemcached(Spymemcached spymemcached) {
		this.spymemcached = spymemcached;
	}	
	
	public Pool getPool() {
		return pool;
	}

	public void setPool(Pool pool) {
		this.pool = pool;
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

	public static CouchBase couchbase() {
		return getInstance().getCouchbase();
	}
	
	public static Spymemcached memcached() {
		return getInstance().getSpymemcached();
	}

	public static Pool pool() {
		return getInstance().getPool();
	}
}
