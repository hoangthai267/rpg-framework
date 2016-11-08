package com.rpg.framework.test;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;

import com.google.protobuf.InvalidProtocolBufferException;

import com.rpg.framework.data.CouchBase;
import com.rpg.framework.data.Pool;
import com.rpg.framework.data.Spymemcached;
import com.rpg.framework.database.Protocol;
import com.rpg.framework.sever.SocketServer;

public class Server extends SocketServer {
	private String host;
	private int port;
	private CouchBase couchbase;
	private Spymemcached spymemcached;
	private Pool pool;

	public Server(String host, int port) {
		super(host, port);
		this.host = host;
		this.port = port;

		this.couchbase = new CouchBase("Static");
		this.spymemcached = new Spymemcached("Dynamic", "");
		this.pool = new Pool(couchbase, spymemcached);
	}

	public static void main(String args[]) throws Exception {
		new Server("localhost", 8463).start();
		System.out.println("Server start.");
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public byte[] handleMessage(int commandID, byte[] data) {
		try {
			switch (commandID) {
			case Protocol.MessageType.REQUEST_LOGIN_VALUE: {
				return handleRequestLogin(Protocol.RequestLogin.parseFrom(data));
			}
			case Protocol.MessageType.REQUEST_REGISTER_VALUE: {
				return handleRequest(Protocol.RequestRegister.parseFrom(data));
			}
			case Protocol.MessageType.REQUEST_GET_CHARACTER_VALUE: {
				return handleRequest(Protocol.RequestGetCharacter.parseFrom(data));
			}
			case Protocol.MessageType.REQUEST_CREATE_CHARACTER_VALUE: {
				return handleRequest(Protocol.RequestCreateCharacter.parseFrom(data));
			}
			case Protocol.MessageType.REQUEST_START_GAME_VALUE: {
				return handleRequest(Protocol.RequestStartGame.parseFrom(data));
			}
			case Protocol.MessageType.REQUEST_UPDATE_POSITION_VALUE: {
				return handleRequest(Protocol.RequestUpdatePosition.parseFrom(data));
			}
			case Protocol.MessageType.REQUEST_GET_ITEMS_VALUE: {
				return handleRequest(Protocol.RequestGetItems.parseFrom(data));
			}
			default:
				break;
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		return data;
	}

	public byte[] handleRequestLogin(Protocol.RequestLogin request) {
		Protocol.ResponseLogin.Builder builder = Protocol.ResponseLogin.newBuilder();
		builder.setResult(Protocol.ResponseCode.FAIL);
		String statement = "SELECT * FROM `Static` s WHERE `username` = \"" + request.getUsername()
				+ "\" AND `password` = \"" + request.getPassword() + "\";";
		N1qlQueryResult queryResult = couchbase.query(statement);
		if (queryResult.allRows().size() == 1) {
			builder.setResult(Protocol.ResponseCode.SUCCESS);
			builder.setUserID(queryResult.rows().next().value().getObject("s").getString("userID"));
			builder.setHasCharacter(queryResult.rows().next().value().getObject("s").getBoolean("hasCharacter"));
		} else {
			builder.setMessage("Invalid username or password.");
			System.out.println(statement);
		}

		return builder.build().toByteArray();
	}

	public byte[] handleRequest(Protocol.RequestRegister request) {
		Protocol.ResponseRegister.Builder builder = Protocol.ResponseRegister.newBuilder();
		String statement = "SELECT * FROM `Static` s WHERE s.username = \"" + request.getUsername() + "\";";

		N1qlQueryResult queryResult = couchbase.query(statement);
		if (!queryResult.allRows().isEmpty()) {
			builder.setResult(Protocol.ResponseCode.FAIL);
			builder.setMessage("Invalid username");
			return builder.build().toByteArray();
		}

		long count = couchbase.counter("index", 1);

		JsonObject user = JsonObject.create().put("username", request.getUsername())
				.put("password", request.getPassword()).put("userID", "User_" + count).put("hasCharacter", false);

		couchbase.set("User_" + count, user);

		builder.setResult(Protocol.ResponseCode.SUCCESS);
		builder.setMessage("Success");

		return builder.build().toByteArray();
	}

	public byte[] handleRequest(Protocol.RequestGetCharacter request) {
		JsonObject stats = couchbase.get(request.getUserID() + "_Character_Stats");
		JsonObject position = couchbase.get(request.getUserID() + "_Character_Position");
		JsonObject status = couchbase.get(request.getUserID() + "_Character_Status");
		Protocol.ResponseGetCharacter reponse = Protocol.ResponseGetCharacter.newBuilder()
				.setResult(Protocol.ResponseCode.SUCCESS)
				.setCharacter(Protocol.Character.newBuilder()
						.setName(stats.getString("name"))
						.setGender(stats.getInt("gender"))
						.setOccupation(stats.getString("occupation"))
						.setLevel(stats.getInt("level"))
						.setStrength(stats.getInt("strength"))
						.setMagic(stats.getInt("magic"))
						.setDefense(stats.getInt("defense"))
						.setSpeed(stats.getInt("speed"))
						.setDame(stats.getInt("dame"))
						.setArmor(stats.getInt("armor"))
						
						.setMapID(position.getString("mapID"))
						.setX(position.getDouble("x"))
						.setY(position.getDouble("y"))
						
						.setMaxHP(status.getInt("maxHP"))
						.setCurHP(status.getInt("curHP"))
						.setMaxMP(status.getInt("maxMP"))
						.setCurMP(status.getInt("curMP"))						
						.build())				
				.build();

		return reponse.toByteArray();
	}

	public byte[] handleRequest(Protocol.RequestCreateCharacter request) {
		JsonObject user = couchbase.get(request.getUserID());
		user.put("hasCharacter", true);
		couchbase.set(request.getUserID(), user);

		JsonObject stats = JsonObject.create().put("name", request.getName()).put("gender", 0).put("occupation", "")
				.put("level", 1).put("strength", 1).put("magic", 1).put("defense", 1).put("speed", 1).put("dame", 1)
				.put("armor", 1);

		JsonObject position = JsonObject.create().put("mapID", "Map_1").put("x", 0.0).put("y", 0.0);

		JsonObject status = JsonObject.create().put("maxHP", 100).put("curHP", 100).put("maxMP", 100).put("curMP", 100);

		JsonObject items = JsonObject.create().put("items", JsonArray.create().add(0).add(1));
		
		JsonObject character = JsonObject.create().put("stats", stats).put("position", position).put("status", status);

		couchbase.set(request.getUserID() + "_Character_Stats", stats);
		couchbase.set(request.getUserID() + "_Character_Position", position);
		couchbase.set(request.getUserID() + "_Character_Status", status);
		couchbase.set(request.getUserID() + "_Character_Items", items);
//		couchbase.set(request.getUserID() + "_Character", character);

		Protocol.ResponseCreateCharacter.Builder builder = Protocol.ResponseCreateCharacter.newBuilder();
		builder.setResult(Protocol.ResponseCode.SUCCESS);

		return builder.build().toByteArray();
	}

	public byte[] handleRequest(Protocol.RequestStartGame request) {
		Protocol.ResponseStartGame.Builder builder = Protocol.ResponseStartGame.newBuilder();
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		builder.setMessage("Welcome to our game.");

		return builder.build().toByteArray();
	}

	public byte[] handleRequest(Protocol.RequestUpdatePosition request) {
		String id = request.getUserID() + "_Character_Position";
		JsonObject characterPosition = JsonObject.create().put("mapID", request.getMapID()).put("x", request.getX())
				.put("y", request.getY());

		pool.set(id, characterPosition);

		Protocol.ResponseUpdatePosition.Builder builder = Protocol.ResponseUpdatePosition.newBuilder();
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		return builder.build().toByteArray();
	}

	public byte[] handleRequest(Protocol.RequestGetItems request) {
		Protocol.ResponseGetItems.Builder builder = Protocol.ResponseGetItems.newBuilder();
		builder.setResult(Protocol.ResponseCode.SUCCESS);

		JsonArray array = couchbase.get(request.getUserID() + "_Character_Items").getArray("items");
		for (int i = 0; i < array.size(); i++ ) {
			builder.addItems(array.getInt(i));
		}
		
		return builder.build().toByteArray();				
	}
}
