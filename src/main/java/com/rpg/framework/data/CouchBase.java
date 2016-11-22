package com.rpg.framework.data;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;

public class CouchBase {	
	private Bucket bucket;

	public CouchBase(String bucketName) {	
		this.bucket = CouchbaseCluster.create( "127.0.0.1").openBucket(bucketName);
	}
	
	public CouchBase(String host, String bucketName) {
		this.bucket = CouchbaseCluster.create(host).openBucket(bucketName);
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
