package com.rpg.framework.core;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;

public class Database {
	private List<URI> 		uris;
	private Queue<String> 	keys;
	private double			cachedTime;
	
	private Bucket 			couchbaseBucket;
	private CouchbaseClient memcachedClient;	
	
	public Database() {
		this.uris = new LinkedList<URI>();
		this.keys = new LinkedList<String>();
		this.cachedTime = 0.0;
		this.uris.add(URI.create("http://127.0.0.1:8091/pools"));

		this.couchbaseBucket = CouchbaseCluster.create("127.0.0.1").openBucket("");
		try {
			this.memcachedClient = new CouchbaseClient(uris, "", "");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

		}
	}

	public Database(String coucbase, String memcached) {
		this.uris = new LinkedList<URI>();
		this.keys = new LinkedList<String>();
		this.cachedTime = 0.0;
		this.uris.add(URI.create("http://127.0.0.1:8091/pools"));

		this.couchbaseBucket = CouchbaseCluster.create("127.0.0.1").openBucket(coucbase);
		try {
			this.memcachedClient = new CouchbaseClient(uris, memcached, "");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setCouchbase(String id, String json) {
		couchbaseBucket.upsert(JsonDocument.create(id, JsonObject.fromJson(json)));
	}
	
	public void setCouchbase(String id, JsonObject value) {
		couchbaseBucket.upsert(JsonDocument.create(id, value));
	}

//	public String getCouchbase(String id) {
//		String result = null;
//		try {
//			result = couchbaseBucket.get(id).content().toString();
//		} catch (Exception ex) {
//			System.out.println("The document's ID doesn't exist : " + id + ".");
//		}
//		return result;
//	}
	
	public JsonObject getCouchbase(String id) {
		JsonObject object = null;
		try {
			object = couchbaseBucket.get(id).content();
		} catch (Exception ex) {
			System.out.println("The document's ID doesn't exist : " + id + ".");
		}
		
		return object;
	}
	
	public List<String> query(String statement) {
		List<String> result = new LinkedList<String>();
		
		N1qlQueryResult rows = couchbaseBucket.query(N1qlQuery.simple(statement));
		for (N1qlQueryRow row : rows) {
			result.add(row.value().toString());
		}	
		
		return result;
	}

	public void setMemcached(String id, String json) {
		if (!keys.contains(id)) {
			keys.add(id);
		}
		memcachedClient.set(id, json);
	}
	
	public void setMemcached(String key, JsonObject value) {
		memcachedClient.set(key, value);
	}
	
//	public String getMemcached(String id) {
//		return (String) memcachedClient.get(id);
//	}
	
	public JsonObject getMemcached(String key) {
		return (JsonObject) memcachedClient.get(key);
	}
}
