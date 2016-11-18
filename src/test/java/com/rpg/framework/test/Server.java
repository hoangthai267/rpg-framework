package com.rpg.framework.test;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.rpg.framework.data.CouchBase;
import com.rpg.framework.data.Pool;
import com.rpg.framework.data.Spymemcached;
import com.rpg.framework.database.Protocol;
import com.rpg.framework.entity.Message;
import com.rpg.framework.entity.Monster;
import com.rpg.framework.entity.User;
import com.rpg.framework.manager.DataManager;
import com.rpg.framework.manager.MapManager;
import com.rpg.framework.manager.MessageManager;
import com.rpg.framework.manager.MonsterManager;
import com.rpg.framework.manager.UserManager;
import com.rpg.framework.sever.SocketServer;

public class Server extends SocketServer {
	private String host;
	private int port;
	private CouchBase couchbase;
	private Spymemcached spymemcached;
	private Pool pool;
	private int currentChannel;
	
	private MapManager mapManager;
	private UserManager userManager;
	private MessageManager messageManager;
	
	public Server(String host, int port) {
		super(host, port);
		this.host = host;
		this.port = port;

		this.couchbase = new CouchBase("Static");
		this.spymemcached = new Spymemcached("Dynamic", "");
		this.pool = new Pool(couchbase, spymemcached);
		
		DataManager.getInstance().setCouchbase(couchbase);
		DataManager.getInstance().setSpymemcached(spymemcached);
		DataManager.getInstance().setPool(pool);
		this.mapManager = MapManager.getInstance();
		this.userManager = UserManager.getInstance();
		this.messageManager = MessageManager.getInstance();
	}
	
	public synchronized boolean start() {
		super.start();
		System.out.println("Start at host: " + host);
		
		loop();
		return true;
	}
	
	public void loop() {
		long lastLoopTime = System.nanoTime();
		final int TARGET_FPS = 5;
		final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
		long lastFpsTime = 0;
		int fps = 0;
		// keep looping round til the game ends
		while (true) {
			// work out how long its been since the last update, this
			// will be used to calculate how far the entities should
			// move this loop
			long now = System.nanoTime();
			long updateLength = now - lastLoopTime;
			lastLoopTime = now;
			
			if(updateLength < OPTIMAL_TIME) {
				long sleepTime = (OPTIMAL_TIME - updateLength) / 1000000;
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				updateLength = OPTIMAL_TIME;
			}
			
			double delta = updateLength / ((double)1000000000);
			// update the frame counter
			lastFpsTime += updateLength;
			fps++;

			// update our FPS counter if a second has passed since
			// we last recorded
			if (lastFpsTime >= 1000000000) {
//				System.out.println("(FPS: " + fps);
				lastFpsTime = 0;
				fps = 0;
			}

			// update the game logic
			update(delta);

			// draw everyting
			// render();
		}
	}
	
	private void update(double delta) {
		List<Message> messages = messageManager.getMessages();
		for(int i = 0; i < messages.size(); i++) {
			Message message = messages.get(i);
			switch (message.getType()) {
				case 1: {
					send(message.getChannelID(), 0, message.getCommandID(), message.getData());
					break;
				}
				case 2: {				
					List<Integer> channels = message.getChannels();
					for (Integer channelID : channels) {
						send(channelID, 0, message.getCommandID(), message.getData());
					}
					break;
				}
				case 3: {		
					send(-1, 2, message.getCommandID(), message.getData());
					break;
				}
				case 4: {				
					handleMessage(message.getChannelID(), message.getCommandID(), message.getData());
					break;
				}			
				default:
					break;
			}
		}		
		mapManager.update(delta);
		MonsterManager.getInstance().update(delta);
		userManager.update(delta);
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

	public void receive(int channelID, int commandID, byte[] data) {
//		System.out.println("Server.receive() channelID: " + channelID + " commandID: " + commandID);
		messageManager.newMessage(Message.RECEIVE, channelID, commandID, data);
	}
	
	public void handleMessage(int channelID, int commandID, byte[] data) {
		currentChannel = channelID;
		try {
			switch (commandID) {
			case Protocol.MessageType.REQUEST_LOGIN_VALUE: {
				send(channelID, 0, Protocol.MessageType.RESPONE_LOGIN_VALUE, handleRequestLogin(Protocol.RequestLogin.parseFrom(data)));
				break;
			}
			case Protocol.MessageType.REQUEST_REGISTER_VALUE: {
				send(channelID, 0, Protocol.MessageType.RESPONE_REGISTER_VALUE, handleRequest(Protocol.RequestRegister.parseFrom(data)));
				break;
			}
			case Protocol.MessageType.REQUEST_GET_CHARACTER_VALUE: {
				send(channelID, 0, Protocol.MessageType.RESPONE_GET_CHARACTER_VALUE, handleRequest(Protocol.RequestGetCharacter.parseFrom(data)));
				break;
			}
			case Protocol.MessageType.REQUEST_CREATE_CHARACTER_VALUE: {
				send(channelID, 0, Protocol.MessageType.RESPONE_CREATE_CHARACTER_VALUE, handleRequest(Protocol.RequestCreateCharacter.parseFrom(data)));
				break;
			}
			case Protocol.MessageType.REQUEST_START_GAME_VALUE: {
				send(channelID, 0, Protocol.MessageType.RESPONE_START_GAME_VALUE, handleRequest(Protocol.RequestStartGame.parseFrom(data)));
				break;
			}
			case Protocol.MessageType.REQUEST_UPDATE_POSITION_VALUE: {
				messageManager.newMessage(Message.SEND_TO_ONE, channelID, Protocol.MessageType.RESPONE_UPDATE_POSITION_VALUE, handleRequest(Protocol.RequestUpdatePosition.parseFrom(data)));
//				send(channelID, 0, Protocol.MessageType.RESPONE_UPDATE_POSITION_VALUE, handleRequest(Protocol.RequestUpdatePosition.parseFrom(data)));
				break;
			}
			case Protocol.MessageType.REQUEST_GET_ITEMS_VALUE: {
				send(channelID, 0, Protocol.MessageType.RESPONSE_GET_ITEMS_VALUE, handleRequest(Protocol.RequestGetItems.parseFrom(data)));
				break;
			}
			case Protocol.MessageType.REQUEST_UPDATE_ACTION_VALUE: {
//				send(channelID, 1, Protocol.MessageType.RESPONSE_UPDATE_ACTION_VALUE, handleRequest(Protocol.RequestUpdateAction.parseFrom(data)));
				handleRequest(Protocol.RequestUpdateAction.parseFrom(data));
				break;
			}
			case Protocol.MessageType.REQUEST_GET_PROTOTYPE_VALUE: {
				send(channelID, 0, Protocol.MessageType.RESPONSE_GET_PROTOTYPE_VALUE, handleRequest(Protocol.RequestGetPrototype.parseFrom(data)));
			}
			default:
				break;
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	public byte[] handleRequest(Protocol.RequestUpdateAction request) {
		int userID = request.getUserID();
		User user = userManager.getIdentifiedUser(userID);
		int mapID = user.getPosition().getMapID();
		
		List<Integer> userList = mapManager.getUserList(mapID);
		
		for (Integer id : userList) {
			User sendUser = userManager.getIdentifiedUser(id.intValue());
			if(id.intValue() != userID) {
				messageManager.newMessage(Message.SEND_TO_ONE, sendUser.getConnectionID(), Protocol.MessageType.RESPONSE_UPDATE_ACTION_VALUE, request.toByteArray());
			}
		}
		
		return null;
	}

	public byte[] handleRequestLogin(Protocol.RequestLogin request) {
		Protocol.ResponseLogin.Builder builder = Protocol.ResponseLogin.newBuilder();
		builder.setResult(Protocol.ResponseCode.FAIL);
		String statement = String.format("SELECT %s.* FROM `Static` as Accounts USE KEYS \"Accounts\" WHERE %s.`password` = \"%s\";", 
				request.getUsername(), 
				request.getUsername(), 
				request.getPassword());
		
		N1qlQueryResult queryResult = couchbase.query(statement);
		if (queryResult.allRows().size() == 1) {
			builder.setResult(Protocol.ResponseCode.SUCCESS);

			int userID = queryResult.rows().next().value().getInt("userID");
			boolean hasCharacter = couchbase.get("User_" + userID).getBoolean("hasCharacter");
			
			builder.setUserID(userID);
			builder.setHasCharacter(hasCharacter);
			
			if(hasCharacter) {
				userManager.addIdentifiedUser(currentChannel, userID);
			}
			
		} else {
			builder.setMessage("Invalid username or password.");
			System.out.println(statement);
		}

		return builder.build().toByteArray();
	}

	public byte[] handleRequest(Protocol.RequestRegister request) {
		Protocol.ResponseRegister.Builder builder = Protocol.ResponseRegister.newBuilder();
		
		JsonObject accounts = couchbase.get("Accounts");
		if(accounts.getObject(request.getUsername()) != null) {
			builder.setResult(Protocol.ResponseCode.FAIL);
			builder.setMessage("Invalid username");
			return builder.build().toByteArray();
		}

		long count = couchbase.counter("index", 1);

		JsonObject user = JsonObject.create().put("username", request.getUsername())
				.put("password", request.getPassword()).put("userID", count).put("hasCharacter", false);

		accounts.put(request.getUsername(), user);
		
		couchbase.set("Accounts", accounts);
		couchbase.set("User_" + count, user);
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		builder.setMessage("Success");

		return builder.build().toByteArray();
	}

	public byte[] handleRequest(Protocol.RequestGetCharacter request) {
		System.out.println(request.getUserID());
		JsonObject stats = couchbase.get("User_" + request.getUserID() + "_Stats");
		JsonObject position = couchbase.get("User_" + request.getUserID() + "_Position");
		JsonObject status = couchbase.get("User_" + request.getUserID() + "_Status");
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
						
						.setMapID(position.getInt("mapID"))
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
		Protocol.ResponseCreateCharacter.Builder builder = Protocol.ResponseCreateCharacter.newBuilder();
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		
		JsonObject user = couchbase.get("User_" + request.getUserID());
		if (!user.getBoolean("hasCharacter")) {
			user.put("hasCharacter", true);
			couchbase.set("User_" + request.getUserID(), user);

			JsonObject stats = JsonObject.create().put("name", request.getName()).put("gender", 0).put("occupation", "")
					.put("level", 1).put("strength", 1).put("magic", 1).put("defense", 1).put("speed", 1).put("dame", 1)
					.put("armor", 1);

			JsonObject position = JsonObject.create().put("mapID", 1).put("x", 0.0).put("y", 0.0);

			JsonObject status = JsonObject.create().put("maxHP", 100).put("curHP", 100).put("maxMP", 100).put("curMP",
					100);

			JsonObject items = JsonObject.create().put("items", JsonArray.create().add(0).add(1));

			// JsonObject character = JsonObject.create().put("stats",
			// stats).put("position", position).put("status", status);

			couchbase.set("User_" + request.getUserID() + "_Stats", stats);
			couchbase.set("User_" + request.getUserID() + "_Position", position);
			couchbase.set("User_" + request.getUserID() + "_Status", status);
			couchbase.set("User_" + request.getUserID() + "_Items", items);
			// couchbase.set(request.getUserID() + "_Character", character);
		}
		userManager.addIdentifiedUser(currentChannel, request.getUserID());
		return builder.build().toByteArray();
	}

	public byte[] handleRequest(Protocol.RequestStartGame request) {
		Protocol.ResponseStartGame.Builder builder = Protocol.ResponseStartGame.newBuilder();
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		builder.setMessage("Welcome to our game.");		
		int mapID = userManager.getIdentifiedUser(request.getUserID()).getPosition().getMapID();
		System.out.println(mapID);
		List<Integer> userList = mapManager.getUserList(mapID);
		System.out.println(userList);
		List<Integer> monsterList = mapManager.getMonsterList(mapID);
//		List<Integer> itemList = mapManager.getItemList(1);
		for (Integer userID : userList) {
			User user = userManager.getIdentifiedUser(userID);
			builder.addUsers(Protocol.User.newBuilder()
					.setId(userID)
					.setPosition(Protocol.Position.newBuilder()
//							.setMapID(user.getPosition().getMapID()))
							.setMapID(1)
							.setX(user.getPosition().getX())
							.setY(user.getPosition().getY())
							)
					.setStatus(Protocol.Status.newBuilder()
							.setMaxHP(user.getStatus().getMaxHP())
							.setCurHP(user.getStatus().getCurHP())
							.setMaxMP(user.getStatus().getMaxMP())
							.setCurMP(user.getStatus().getCurMP())
							)
					.setStats(Protocol.Stats.newBuilder()
							.setDamage(user.getStats().getDamage())
							.setDefense(user.getStats().getDefense())
							.setSpeed(user.getStats().getSpeed()))
					);			
		}
		
		for (int i = 0; i < monsterList.size(); i++) {
			
			Monster entity = MonsterManager.getInstance().getMonsterInList(monsterList.get(i));
			
			builder.addMonsters(Protocol.Monster.newBuilder()
					.setId(entity.getId())
					.setIndex(entity.getIndex())
					.setPosition(Protocol.Position.newBuilder()
							.setMapID(1)
							.setX(entity.getPosition().getX())
							.setY(entity.getPosition().getY()))
					.setStats(Protocol.Stats.newBuilder()
							.setDamage(entity.getStats().getDamage())
							.setDefense(entity.getStats().getDefense())
							.setSpeed(entity.getStats().getSpeed()))
					.setStatus(Protocol.Status.newBuilder()
							.setCurHP(entity.getStatus().getCurHP())
							.setCurMP(entity.getStatus().getCurMP())
							.setMaxHP(entity.getStatus().getMaxHP())
							.setMaxMP(entity.getStatus().getMaxMP()))
					);
		}
		
		
		mapManager.enterMap(request.getUserID(), mapID);
		
		

		return builder.build().toByteArray();
	}

	public byte[] handleRequest(Protocol.RequestUpdatePosition request) {
		String id = "User_" + request.getUserID() + "_Position";
		JsonObject characterPosition = JsonObject.create().put("mapID", request.getMapID()).put("x", request.getX())
				.put("y", request.getY());
		
		userManager.getIdentifiedUser(request.getUserID()).getPosition().set(request.getMapID(), request.getX(), request.getY());
		mapManager.sendMessageUpdateUser(request.getMapID(), request.getUserID(), request.getX(), request.getY());
		
		pool.set(id, characterPosition);

		Protocol.ResponseUpdatePosition.Builder builder = Protocol.ResponseUpdatePosition.newBuilder();
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		return builder.build().toByteArray();
	}

	public byte[] handleRequest(Protocol.RequestGetItems request) {
		Protocol.ResponseGetItems.Builder builder = Protocol.ResponseGetItems.newBuilder();
		builder.setResult(Protocol.ResponseCode.SUCCESS);

		JsonArray array = couchbase.get("User_" + request.getUserID() + "_Items").getArray("items");
		for (int i = 0; i < array.size(); i++ ) {
			builder.addItems(array.getInt(i));
		}
		
		return builder.build().toByteArray();				
	}
	
	public byte[] handleRequest(Protocol.RequestGetPrototype request) {
		Protocol.ResponseGetPrototype.Builder builder = Protocol.ResponseGetPrototype.newBuilder();
		JsonFormat.Parser parser = JsonFormat.parser();
		
		JsonObject items = couchbase.get("Prototype_Items");
		JsonArray useItems = items.getArray("use");
		for (int i = 0; i < useItems.size(); i++ ) {
			String json = useItems.getObject(i).toString();
			Protocol.Use.Builder newBuilder = Protocol.Use.newBuilder();
			try {
				parser.merge(json, newBuilder);
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			builder.addItems(Protocol.Item.newBuilder()
					.setType(Protocol.ItemType.ITEM_TYPE_USE)
					.setData(newBuilder.build().toByteString())
					.build());
		}
		
		JsonArray collectItems = items.getArray("collect");
		for (int i = 0; i < collectItems.size(); i++ ) {	
			String json = collectItems.getObject(i).toString();
			Protocol.Collect.Builder newBuilder = Protocol.Collect.newBuilder();
			try {
				parser.merge(json, newBuilder);
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			builder.addItems(Protocol.Item.newBuilder()
					.setType(Protocol.ItemType.ITEM_TYPE_COLLECT)
					.setData(newBuilder.build().toByteString())
					.build());
		}
		
		JsonArray equipItems = items.getArray("equip");
		for (int i = 0; i < equipItems.size(); i++ ) {
			String json = equipItems.getObject(i).toString();
			Protocol.Equip.Builder newBuilder = Protocol.Equip.newBuilder();
			try {
				parser.merge(json, newBuilder);
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			builder.addItems(Protocol.Item.newBuilder()
					.setType(Protocol.ItemType.ITEM_TYPE_EQUIP)
					.setData(newBuilder.build().toByteString())
					.build());
			
			System.out.println(builder.build().toString());
		}
		return builder.build().toByteArray();
	}
	
	public void activeConnection(int connectionID) {
		userManager.addAnonymousUser(connectionID);
	}
	
	public void inactiveConnection(int connectionID) {
		if(!userManager.removeIdentifiedUser(connectionID))
			userManager.removeAnonymousUser(connectionID);
	}
}
