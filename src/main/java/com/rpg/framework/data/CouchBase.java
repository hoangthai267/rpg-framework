package com.rpg.framework.data;

import java.util.*;

import javax.management.Query;

import com.couchbase.client.java.*;
import com.couchbase.client.java.document.*;
import com.couchbase.client.java.document.json.*;
import com.couchbase.client.java.query.N1qlQuery;

import static com.couchbase.client.java.query.dsl.Expression.i;
import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.x;
import com.couchbase.client.*;
import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonParser;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.query.dsl.path.index.CreateIndexPath;


import java.io.IOException;
import java.net.URI;
import com.couchbase.client.CouchbaseClient;
import net.spy.memcached.internal.OperationFuture;

public class CouchBase {
	private static CouchBase Instance = null;
	
	private Cluster cluster;
	private Bucket staticBucket;
	private Bucket dynamicBucket;
	private String index;
	private Timer refreshTimer;
	private Queue<String> updatedPositionPoll;
	
	public CouchBase() {
		this.cluster = CouchbaseCluster.create("localhost");
		this.staticBucket = this.cluster.openBucket("Static");
//		this.dynamicBucket = this.cluster.openBucket("Dynamic");
		this.index = "index";
		this.updatedPositionPoll = new LinkedList<String>();
		this.refreshTimer = new Timer();
		refreshTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				
				// TODO Auto-generated method stub
				for (int i = 0; i < 2000; i++) {//					
					String id = updatedPositionPoll.poll();
					if(id == null)
						break;
					JsonObject object = JsonObject.fromJson(Spymemcached.getInstance().get(id).toString());
					staticBucket.upsert(JsonDocument.create(id, object));
				}
			}
		}, 1000, 1000);
	}
	
	public static CouchBase getInstance() {
		if(Instance == null)
			Instance = new CouchBase();
		return Instance;
	}
	
	public Protocol.ResponseLogin handleRequest(Protocol.RequestLogin request) {
		Protocol.ResponseLogin.Builder builder = Protocol.ResponseLogin.newBuilder();
		builder.setResult(Protocol.ResponseCode.FAIL);
		String statement = "SELECT * FROM `Static` s WHERE `username` = \"" + request.getUsername() + "\" AND `password` = \"" + request.getPassword() + "\";";
		N1qlQueryResult queryResult = staticBucket.query(N1qlQuery.simple(statement));
		if(queryResult.allRows().size() == 1) {
			builder.setResult(Protocol.ResponseCode.SUCCESS);
			builder.setUserID(queryResult.rows().next().value().getObject("s").getString("userID"));
		} else {
			builder.setMessage("Invalid username or password.");
			System.out.println("Username: " + request.getUsername() + " password: " + request.getPassword());
			System.out.println(statement);
		}
		
		return builder.build();
	}
	
	public Protocol.ResponseRegister handleRequest(Protocol.RequestRegister request) {
		Protocol.ResponseRegister.Builder builder = Protocol.ResponseRegister.newBuilder();
		String statement = "SELECT * FROM `Static` s WHERE s.username = \"" + request.getUsername() + "\";";
		
		N1qlQueryResult queryResult = staticBucket.query(N1qlQuery.simple(statement));
		if(!queryResult.allRows().isEmpty()) {
			builder.setResult(Protocol.ResponseCode.FAIL);
			builder.setMessage("Invalid username");
			return builder.build();
		}

		long count = 0;
		
		try {
			count = staticBucket.counter(index, 1).content();
		}
		catch (Exception ex) {
			count = staticBucket.counter(index, 0, 1).content();
		}
		
		JsonObject user = JsonObject.create()
				.put("username", request.getUsername())
				.put("password", request.getPassword())
				.put("userID", "User_" + count)
				.put("numberOfCharacter", 0);
		
		staticBucket.upsert(JsonDocument.create("User_" + count, user));		
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		builder.setMessage("Success");
		
		return builder.build();
	}
	
	public Protocol.ResponseListOfCharacter handleRequest(Protocol.RequestListOfCharacter request) {
		int numberOfCharacter = staticBucket.get(request.getUserID()).content().getInt("numberOfCharacter");
		Protocol.ResponseListOfCharacter.Builder builder = Protocol.ResponseListOfCharacter.newBuilder();
		
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		builder.setNumberOfCharacter(numberOfCharacter);
		for (int i = 1; i <= numberOfCharacter; i++) {
			
			JsonObject data 	= staticBucket.get(request.getUserID() + "_Character_" + i + "_Data").content();
			JsonObject position = staticBucket.get(request.getUserID() + "_Character_" + i + "_Position").content();
			JsonObject status 	= staticBucket.get(request.getUserID() + "_Character_" + i + "_Status").content();
			builder.addListOfCharacter(Protocol.Character.newBuilder()
					.setData(Protocol.CharacterData.newBuilder()
							.setName(data.getString("name"))
							.setOccupation(data.getString("occupation"))
							.setLevel(data.getInt("level"))
							.setStrength(data.getInt("strength"))
							.setMagic(data.getInt("magic"))
							.setDefense(data.getInt("defense"))
							.setSpeed(data.getInt("speed"))
							.setDame(data.getInt("dame"))
							.setArmor(data.getInt("armor")))
					.setPosition(Protocol.CharacterPosition.newBuilder()
							.setMapID(position.getString("mapID"))
							.setX(position.getDouble("x"))
							.setY(position.getDouble("y")))
					.setStatus(Protocol.CharacterStatus.newBuilder()
							.setCurHP(status.getInt("curHP"))
							.setMaxHP(status.getInt("maxHP"))
							.setCurMP(status.getInt("curMP"))
							.setMaxMP(status.getInt("maxMP")))
					.setID("Character_" + i)
					);
		}
				
		return builder.build();
	}
	
	public Protocol.ResponseCreateCharacter handleRequest(Protocol.RequestCreateCharacter request) {		
		JsonObject user = staticBucket.get(request.getUserID()).content();
		int numberOfCharacter = user.getInt("numberOfCharacter");
		numberOfCharacter++;
		user.put("numberOfCharacter", numberOfCharacter);
		staticBucket.replace(JsonDocument.create(request.getUserID(), user));
		
		JsonObject characterData = JsonObject.create()
				.put("name", request.getName())
				.put("occupation", request.getOccupation())				
				.put("level", 1)
				.put("strength", 1)
				.put("magic", 1)
				.put("defense", 1)
				.put("speed", 1)
				.put("dame", 1)
				.put("armor", 1);		
		staticBucket.upsert(JsonDocument.create(request.getUserID() + "_Character_" + numberOfCharacter + "_Data", characterData));
		
		JsonObject characterPosition = JsonObject.create()
				.put("mapID", "Map_1")
				.put("x", 0.0)
				.put("y", 0.0);		
		staticBucket.upsert(JsonDocument.create(request.getUserID() + "_Character_" + numberOfCharacter + "_Position", characterPosition));
		
		JsonObject characterStatus = JsonObject.create()
				.put("maxHP", 100)
				.put("curHP", 100)
				.put("maxMP", 100)
				.put("curMP", 100);		
		staticBucket.upsert(JsonDocument.create(request.getUserID() + "_Character_" + numberOfCharacter + "_Status", characterStatus));
		
		
		Protocol.ResponseCreateCharacter.Builder builder = Protocol.ResponseCreateCharacter.newBuilder();
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		
		return builder.build();
	}
	
	public Protocol.ResponseStartGame handleRequest(Protocol.RequestStartGame request) {
		
		return null;
	}
	
	public Protocol.ResponseUpdatePosition handleRequest(Protocol.RequestUpdatePosition request) {
		String id = request.getUserID() + "_" + request.getCharID() + "_Position";
		updatedPositionPoll.add(id);
		JsonObject characterPosition = JsonObject.create()
				.put("mapID", request.getNewPosition().getMapID())
				.put("x", request.getNewPosition().getX())
				.put("y", request.getNewPosition().getY());		
//		dynamicBucket.upsert(JsonDocument.create(id, characterPosition));
		Spymemcached.getInstance().set(id, characterPosition.toString());
		Protocol.ResponseUpdatePosition.Builder builder = Protocol.ResponseUpdatePosition.newBuilder();
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		return builder.build();
	}
	
	public void test() {
		String statement = "SELECT * FROM `Dynamic` d;";
		
		N1qlQueryResult queryResult = dynamicBucket.query(N1qlQuery.simple(statement));
		for (N1qlQueryRow row : queryResult) {
			System.out.println(row);
		}
	}
	
	public static void main(String... args) throws Exception {
		CouchBase.getInstance().test();
	}
}
