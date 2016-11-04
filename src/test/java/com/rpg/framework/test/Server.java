package com.rpg.framework.test;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;

import com.google.protobuf.InvalidProtocolBufferException;

import com.rpg.framework.data.CouchBase;
import com.rpg.framework.data.Spymemcached;
import com.rpg.framework.database.Protocol;
import com.rpg.framework.sever.SocketServer;

public class Server extends SocketServer {
	private String host;
	private int port;
	private CouchBase couchbase;
	private Spymemcached spymemcached;

	public Server(String host, int port) {
		super(host, port);
		this.host = host;
		this.port = port;
		
		this.couchbase = new CouchBase("Static");
		this.spymemcached = new Spymemcached("Dynamic", "");
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
			case Protocol.MessageType.REQUEST_LIST_OF_CHARACTER_VALUE: {
				return handleRequest(Protocol.RequestListOfCharacter.parseFrom(data));
			}			
			case Protocol.MessageType.REQUEST_CREATE_CHARACTER_VALUE: {
				return handleRequest(Protocol.RequestCreateCharacter.parseFrom(data));
			}
			case Protocol.MessageType.REQUEST_START_GAME_VALUE: {
				return handleRequest(Protocol.RequestStartGame.parseFrom(data));
			}

			default:
				break;
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		
		return data;
	}
	
	public byte[] handleRequestLogin(Protocol.RequestLogin request)	 {
		Protocol.ResponseLogin.Builder builder = Protocol.ResponseLogin.newBuilder();
		builder.setResult(Protocol.ResponseCode.FAIL);
		String statement = "SELECT * FROM `Static` s WHERE `username` = \"" + request.getUsername() + "\" AND `password` = \"" + request.getPassword() + "\";";
		N1qlQueryResult queryResult = couchbase.query(statement);
		if(queryResult.allRows().size() == 1) {
			builder.setResult(Protocol.ResponseCode.SUCCESS);
			builder.setUserID(queryResult.rows().next().value().getObject("s").getString("userID"));
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
		if(!queryResult.allRows().isEmpty()) {
			builder.setResult(Protocol.ResponseCode.FAIL);
			builder.setMessage("Invalid username");
			return builder.build().toByteArray();
		}
		
		long count = couchbase.counter("index", 1);
		
		JsonObject user = JsonObject.create()
				.put("username", request.getUsername())
				.put("password", request.getPassword())
				.put("userID", "User_" + count)
				.put("numberOfCharacter", 0);
		
		couchbase.set("User_" + count, user);		
		
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		builder.setMessage("Success");
		
		return builder.build().toByteArray();
	}
	
	public byte[] handleRequest(Protocol.RequestListOfCharacter request) {
		int numberOfCharacter = couchbase.get(request.getUserID()).getInt("numberOfCharacter");
		Protocol.ResponseListOfCharacter.Builder builder = Protocol.ResponseListOfCharacter.newBuilder();
		
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		builder.setNumberOfCharacter(numberOfCharacter);
		for (int i = 1; i <= numberOfCharacter; i++) {
			
			JsonObject data 	= couchbase.get(request.getUserID() + "_Character_" + i + "_Data");
			JsonObject position = couchbase.get(request.getUserID() + "_Character_" + i + "_Position");
			JsonObject status 	= couchbase.get(request.getUserID() + "_Character_" + i + "_Status");
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
				
		return builder.build().toByteArray();
	}
	
	public byte[] handleRequest(Protocol.RequestCreateCharacter request) {	
		JsonObject user = couchbase.get(request.getUserID());
		int numberOfCharacter = user.getInt("numberOfCharacter");
		numberOfCharacter++;
		user.put("numberOfCharacter", numberOfCharacter);
		couchbase.set(request.getUserID(), user);
		
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
		couchbase.set(request.getUserID() + "_Character_" + numberOfCharacter + "_Data", characterData);
		
		JsonObject characterPosition = JsonObject.create()
				.put("mapID", "Map_1")
				.put("x", 0.0)
				.put("y", 0.0);		
		couchbase.set(request.getUserID() + "_Character_" + numberOfCharacter + "_Position", characterPosition);
		
		JsonObject characterStatus = JsonObject.create()
				.put("maxHP", 100)
				.put("curHP", 100)
				.put("maxMP", 100)
				.put("curMP", 100);		
		couchbase.set(request.getUserID() + "_Character_" + numberOfCharacter + "_Status", characterStatus);
		
		
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
}
