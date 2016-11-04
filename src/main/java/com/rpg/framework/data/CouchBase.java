package com.rpg.framework.data;

import com.couchbase.client.java.*;
import com.couchbase.client.java.document.*;
import com.couchbase.client.java.document.json.*;
import com.couchbase.client.java.query.*;


public class CouchBase {	
	private Bucket bucket;

	public CouchBase(String bucketName) {
		this.bucket = CouchbaseCluster.create("localhost").openBucket(bucketName);
	}
	
	public void set(String key, JsonObject value) {
		bucket.upsert(JsonDocument.create(key, value));
	}
	
	public JsonObject get(String id) {
		return bucket.get(id).content();
	}
	
	public long counter(String key, long value) {
		try {
			return bucket.counter(key, value).content();
		}
		catch (Exception ex) {
			return bucket.counter(key, value, 0).content();
		}
	}
	
	public N1qlQueryResult query(String statement) {
		return bucket.query(N1qlQuery.simple(statement));
	}

	public void test() {
	}

	public static void main(String args[]) throws Exception {	
		new CouchBase("Static").test();
		System.exit(0);
	}
}
