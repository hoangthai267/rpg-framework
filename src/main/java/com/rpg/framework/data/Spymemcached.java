package com.rpg.framework.data;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import com.couchbase.client.CouchbaseClient;
import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationFuture;

public class Spymemcached {

	private List<URI> uris;
	private CouchbaseClient client;

	private static Spymemcached instance;

	public static Spymemcached getInstance() {
		if (instance == null)
			instance = new Spymemcached();
		return instance;
	}

	// Hard-code constructor for demo purpose
	private Spymemcached() {
		uris = new LinkedList<URI>();
		// Connect to localhost or to the appropriate URI
		uris.add(URI.create("http://127.0.0.1:8091/pools"));

		try {
			client = new CouchbaseClient(uris, "Dynamic", "");
		} catch (Exception e) {
			System.err.println("Error connecting to Couchbase: " + e.getMessage());
			System.exit(0);
		}
	}

	public Spymemcached(String bucketName, String password) {
		uris = new LinkedList<URI>();
		// Connect to localhost or to the appropriate URI
		uris.add(URI.create("http://127.0.0.1:8091/pools"));

		try {
			client = new CouchbaseClient(uris, bucketName, password);
		} catch (Exception e) {
			System.err.println("Error connecting to Couchbase: " + e.getMessage());
			System.exit(0);
		}
	}

	public void example(boolean do_delete) {
		String KEY = "Key";
		String VALUE = "Hello Spymemcached";
		int EXP_TIME = 10;
		// Do an asynchronous set
		OperationFuture<Boolean> setOp = client.set(KEY, EXP_TIME, VALUE);
		// Do a synchrononous get
		Object getObject = client.get(KEY);
		// Do an asynchronous get
		GetFuture<Object> getOp = client.asyncGet(KEY);
		// Do an asynchronous delete
		OperationFuture<Boolean> delOp = null;
		if (do_delete) {
			delOp = client.delete(KEY);
		}
		// Shutdown the client
		client.shutdown(3, TimeUnit.SECONDS);
		// Now we want to see what happened with our data
		// Check to see if our set succeeded
		try {
			if (setOp.get().booleanValue()) {
				System.out.println("Set Succeeded");
			} else {
				System.err.println("Set failed: " + setOp.getStatus().getMessage());
			}
		} catch (Exception e) {
			System.err.println("Exception while doing set: " + e.getMessage());
		}
		// Print the value from synchronous get
		if (getObject != null) {
			System.out.println("Synchronous Get Suceeded: " + (String) getObject);
		} else {
			System.err.println("Synchronous Get failed");
		}
		// Check to see if ayncGet succeeded
		try {
			if ((getObject = getOp.get()) != null) {
				System.out.println("Asynchronous Get Succeeded: " + getObject);
			} else {
				System.err.println("Asynchronous Get failed: " + getOp.getStatus().getMessage());
			}
		} catch (Exception e) {
			System.err.println("Exception while doing Aynchronous Get: " + e.getMessage());
		}
		// Check to see if our delete succeeded
		if (do_delete) {
			try {
				if (delOp.get().booleanValue()) {
					System.out.println("Delete Succeeded");
				} else {
					System.err.println("Delete failed: " + delOp.getStatus().getMessage());
				}
			} catch (Exception e) {
				System.err.println("Exception while doing delete: " + e.getMessage());
			}
		}
	}

	public void set(String key, String value) {
		OperationFuture<Boolean> setOp = client.set(key, value);
		// Check to see if our set succeeded
		try {
			if (setOp.get().booleanValue()) {
				System.out.println("Set Succeeded");
			} else {
				System.err.println("Set failed: " + setOp.getStatus().getMessage());
			}
		} catch (Exception e) {
			System.err.println("Exception while doing set: " + e.getMessage());
		}
	}

	public void set(String key, String value, int expTime) {
		OperationFuture<Boolean> setOp = client.set(key, expTime, value);
		// Check to see if our set succeeded
		try {
			if (setOp.get().booleanValue()) {
				System.out.println("Set Succeeded");
			} else {
				System.err.println("Set failed: " + setOp.getStatus().getMessage());
			}
		} catch (Exception e) {
			System.err.println("Exception while doing set: " + e.getMessage());
		}
	}

	public void clear() {
		OperationFuture<Boolean> flushOP = client.flush();
		try {
			if (flushOP.get().booleanValue()) {
				System.out.println("Flush: Completed");
			} else {
				System.out.println("Flush: Not compleled");
			}
		} catch (InterruptedException e) {
			System.out.println("Flush error: " + e.toString());
		} catch (ExecutionException e) {
			System.out.println("Flush error: " + e.toString());
		}
	}

	public Object get(String key) {
		return client.get(key);
	}

	public static void main(String[] args) {
		new Spymemcached("Dynamic", "").example(false);
	}
}
