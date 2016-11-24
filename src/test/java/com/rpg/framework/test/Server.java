package com.rpg.framework.test;

import java.util.List;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.rpg.framework.database.Protocol;
import com.rpg.framework.entity.Message;
import com.rpg.framework.entity.Monster;
import com.rpg.framework.entity.User;
import com.rpg.framework.manager.DataManager;
import com.rpg.framework.manager.MapManager;
import com.rpg.framework.manager.MessageManager;
import com.rpg.framework.manager.MonsterManager;
import com.rpg.framework.manager.UserManager;

public class Server extends com.rpg.framework.core.Server {
	
	@Override
	public void start(String host, int port) {		
		DataManager.getInstance();
		MapManager.getInstance();
		MonsterManager.getInstance();
		UserManager.getInstance();		
		MessageManager.getInstance();
		
		super.start(host, port);
	}
	
	@Override
	public void connectedClient(int clientID) {
		System.out.println("Connected client: " + clientID);
		UserManager.getInstance().addAnonymousUser(clientID);
	}
	
	@Override
	public void disconnectedClient(int clientID) {
		System.out.println("Disconnected client: " + clientID);
		if(!UserManager.getInstance().removeIdentifiedUser(clientID))
			UserManager.getInstance().removeAnonymousUser(clientID);
	}
	
	@Override
	public void receiveMessageFrom(int clientID, int messageID, byte[] data) {
		MessageManager.getInstance().receiveMessage(clientID, messageID, data);
	}
	
	@Override
	public void updateSecond(double delta, int fps) {		
		super.updateSecond(delta, fps);
		System.out.print("FPS : " + fps + " ");
		MessageManager.getInstance().print();
	}
	
	@Override
	public void update(double delta) {
		super.update(delta);
		DataManager.getInstance().update(delta);
		MapManager.getInstance().update(delta);
		MonsterManager.getInstance().update(delta);
		UserManager.getInstance().update(delta);		
		MessageManager.getInstance().update(delta);
		
		List<Message> messages = MessageManager.getInstance().getMessages();
		for (Message message : messages) {
			if(message == null)
				continue;
			switch (message.getType()) {
				// send to one
				case 1: {
					if(!sendMessageTo(message.getChannelID(), message.getCommandID(), message.getData()))
						MessageManager.getInstance().newMessage(message);
					break;
				}
				// send to other
				case 2: {
					sendMessageToList(message.getChannels(), message.getCommandID(), message.getData());
					break;				
				}
				// send to all
				case 3: {
					sendMessageToAll(message.getCommandID(), message.getData());
					break;
				}
				// receive
				case 4: {
					handleRequest(message.getChannelID(), message.getCommandID(), message.getData());
					break;
				}	
				default: {
					break;
				}
			}
		}
	}	
		
	private void handleRequest(int clientID, int messageID, byte[] data) {
		try {

			switch (messageID) {
				case Protocol.MessageType.REQUEST_LOGIN_VALUE: {
					handleRequestLogin(clientID, Protocol.RequestLogin.parseFrom(data));
					break;
				}
	
				case Protocol.MessageType.REQUEST_REGISTER_VALUE: {
					handleRequestRegister(clientID, Protocol.RequestRegister.parseFrom(data));
					break;
				}
	
				case Protocol.MessageType.REQUEST_GET_CHARACTER_VALUE: {
					handleRequestGetCharacter(clientID, Protocol.RequestGetCharacter.parseFrom(data));
					break;
				}
	
				case Protocol.MessageType.REQUEST_CREATE_CHARACTER_VALUE: {
					handleRequestCreateCharacter(clientID, Protocol.RequestCreateCharacter.parseFrom(data));
					break;
				}
				
				case Protocol.MessageType.REQUEST_START_GAME_VALUE: {
					handleRequestStartGame(clientID, Protocol.RequestStartGame.parseFrom(data));
					break;
				}
				
				case Protocol.MessageType.REQUEST_UPDATE_POSITION_VALUE: {
					handleRequestUpdatePosition(clientID, Protocol.RequestUpdatePosition.parseFrom(data));
					break;
				}
				
				case Protocol.MessageType.REQUEST_GET_ITEMS_VALUE: {
					handleRequestGetItem(clientID, Protocol.RequestGetItems.parseFrom(data));
					break;
				}
				
				case Protocol.MessageType.REQUEST_UPDATE_ACTION_VALUE: {
					handleRequestUpdateAction(clientID, Protocol.RequestUpdateAction.parseFrom(data));
					break;
				}
				case Protocol.MessageType.REQUEST_GET_PROTOTYPE_VALUE: {
					handleRequestGetPrototype(clientID, Protocol.RequestGetPrototype.parseFrom(data));
					break;
				}
	
				default: {
					break;
				}
			}

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	private void handleRequestLogin(int clientID, Protocol.RequestLogin request) {
		Protocol.ResponseLogin.Builder builder = Protocol.ResponseLogin.newBuilder();
		
		JsonObject accounts = DataManager.getInstance().get("Accounts");
		if(accounts.containsKey(request.getUsername())) {
			JsonObject user = accounts.getObject(request.getUsername());
			String userName = user.getString("userName");
			String password = user.getString("password");
			int	userID 		= user.getInt("userID");
			
			if(password.compareTo(request.getPassword()) == 0) {
				user = DataManager.getInstance().get("User_" + userID);
				
				boolean hasLogin 		= user.getBoolean("hasLogin");
				boolean hasCharacter 	= user.getBoolean("hasCharacter");				
				
				if(hasLogin) {
					builder.setResult(Protocol.ResponseCode.FAIL);
					builder.setMessage("The account has been logged.");
					builder.setUserID(userID);
					builder.setHasCharacter(hasCharacter);
				} else {
					builder.setResult(Protocol.ResponseCode.SUCCESS);
					builder.setMessage("The account are logged.");
					builder.setUserID(userID);
					builder.setHasCharacter(hasCharacter);
					
					if(hasCharacter) {
						UserManager.getInstance().addIdentifiedUser(clientID, userID);
						user.put("hasLogin", true);
						DataManager.getInstance().set("User_" + userID, user);
					} else {
						builder.setMessage("The acconnt don't have a character");
					}
				}
				
			} else {
				builder.setResult(Protocol.ResponseCode.FAIL);
				builder.setMessage("Invalid password.");
			}			
		} else {
			builder.setResult(Protocol.ResponseCode.FAIL);
			builder.setMessage("Invalid username.");
		}
		
		sendMessageTo(clientID, Protocol.MessageType.RESPONE_LOGIN_VALUE, builder.build().toByteArray());
	}
	
	private void handleRequestRegister(int clientID, Protocol.RequestRegister request) {
		Protocol.ResponseRegister.Builder builder = Protocol.ResponseRegister.newBuilder();
		
		JsonObject accounts = DataManager.getInstance().get("Accounts");
		if(accounts.getObject(request.getUsername()) != null) {
			builder.setResult(Protocol.ResponseCode.FAIL);
			builder.setMessage("Invalid username");
		} else {
			long count = accounts.getInt("total");

			JsonObject user = JsonObject.create()
					.put("username", request.getUsername())
					.put("password", request.getPassword())
					.put("userID", count)
					.put("hasCharacter", false)
					.put("hasLogin", false);

			DataManager.getInstance().set("User_" + count, user);
			
			accounts.put(request.getUsername(), user.removeKey("hasCharacter").removeKey("hasLogin"));
			accounts.put("total", count + 1);
			DataManager.getInstance().set("Accounts", accounts);
			
			builder.setResult(Protocol.ResponseCode.SUCCESS);
			builder.setMessage("Success");
		}
		sendMessageTo(clientID, Protocol.MessageType.RESPONE_REGISTER_VALUE, builder.build().toByteArray());
	}
	
	private void handleRequestGetCharacter(int clientID, Protocol.RequestGetCharacter request) {
		JsonObject stats = DataManager.getInstance().get("User_" + request.getUserID() + "_Stats");
		JsonObject position = DataManager.getInstance().get("User_" + request.getUserID() + "_Position");
		JsonObject status = DataManager.getInstance().get("User_" + request.getUserID() + "_Status");
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
		
		sendMessageTo(clientID, Protocol.MessageType.RESPONE_GET_CHARACTER_VALUE, reponse.toByteArray());

	}
	
	private void handleRequestCreateCharacter(int clientID, Protocol.RequestCreateCharacter request) {
		Protocol.ResponseCreateCharacter.Builder builder = Protocol.ResponseCreateCharacter.newBuilder();
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		
		JsonObject user = DataManager.getInstance().get("User_" + request.getUserID());
		if (!user.getBoolean("hasCharacter")) {
			user.put("hasCharacter", true);
			DataManager.getInstance().set("User_" + request.getUserID(), user);

			JsonObject stats = JsonObject.create()
					.put("name", request.getName())
					.put("gender", 0)
					.put("occupation", "")
					.put("level", 1)
					.put("strength", 1)
					.put("magic", 1)
					.put("defense", 1)
					.put("speed", 1)
					.put("dame", 1)
					.put("armor", 1);

			JsonObject position = JsonObject.create()
					.put("mapID", 1)
					.put("x", 0.0)
					.put("y", 0.0);

			JsonObject status = JsonObject.create()
					.put("maxHP", 100)
					.put("curHP", 100)
					.put("maxMP", 100)
					.put("curMP", 100);

			JsonObject items = JsonObject.create().put("items", JsonArray.create().add(0).add(1));

			// JsonObject character = JsonObject.create().put("stats",
			// stats).put("position", position).put("status", status);

			DataManager.getInstance().set("User_" + request.getUserID() + "_Stats", stats);
			DataManager.getInstance().set("User_" + request.getUserID() + "_Position", position);
			DataManager.getInstance().set("User_" + request.getUserID() + "_Status", status);
			DataManager.getInstance().set("User_" + request.getUserID() + "_Items", items);
			// couchbase.set(request.getUserID() + "_Character", character);
			UserManager.getInstance().addIdentifiedUser(clientID, request.getUserID());
		}
		sendMessageTo(clientID, Protocol.MessageType.RESPONE_CREATE_CHARACTER_VALUE, builder.build().toByteArray());
	}
	
	private void handleRequestStartGame(int clientID, Protocol.RequestStartGame request) {
		Protocol.ResponseStartGame.Builder builder = Protocol.ResponseStartGame.newBuilder();
		
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		builder.setMessage("Welcome to our game.");		
		
		int mapID = UserManager.getInstance().getIdentifiedUser(request.getUserID()).getPosition().getMapID();
		
		List<Integer> userList 		= MapManager.getInstance().getUserList(mapID);
		List<Integer> monsterList 	= MapManager.getInstance().getMonsterList(mapID);
//		List<Integer> itemList 		= MapManager.getInstance().getItemList(mapID);
		
		for (Integer userID : userList) {
			User user = UserManager.getInstance().getIdentifiedUser(userID);
			
			builder.addUsers(Protocol.User.newBuilder()
					.setId(userID)
					.setPosition(Protocol.Position.newBuilder()
							.setMapID(user.getPosition().getMapID())
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
		
		
		MapManager.getInstance().enterMap(request.getUserID(), mapID);
		
		sendMessageTo(clientID, Protocol.MessageType.RESPONE_START_GAME_VALUE, builder.build().toByteArray());
	}
	
	private void handleRequestUpdatePosition(int clientID, Protocol.RequestUpdatePosition request) {
		try {
		String id = "User_" + request.getUserID() + "_Position";
		
		JsonObject characterPosition = JsonObject.create()
				.put("mapID", request.getMapID())
				.put("x", request.getX())
				.put("y", request.getY());
		
		User user = UserManager.getInstance().getIdentifiedUser(request.getUserID());
		
		if(user == null)
			return;
		
		user.getPosition().set(request.getMapID(), request.getX(), request.getY());
		
//		MapManager.getInstance().sendMessageUpdateUser(request.getMapID(), request.getUserID(), request.getX(), request.getY(), request.getState());
		DataManager.getInstance().cached(id, characterPosition);
		
		Protocol.ResponseUpdatePosition.Builder builder = Protocol.ResponseUpdatePosition.newBuilder();
		builder.setResult(Protocol.ResponseCode.SUCCESS);
		
		sendMessageTo(clientID, Protocol.MessageType.RESPONE_UPDATE_POSITION_VALUE, builder.build().toByteArray());
		} catch (Exception ex) {
			System.out.println(ex.getMessage() + "handleRequestUpdatePosition userID :" + request.getUserID());
		}
	}
	
	private void handleRequestGetItem(int clientID, Protocol.RequestGetItems request) {
		Protocol.ResponseGetItems.Builder builder = Protocol.ResponseGetItems.newBuilder();
		
		builder.setResult(Protocol.ResponseCode.SUCCESS);

		JsonArray array = DataManager.getInstance().get("User_" + request.getUserID() + "_Items").getArray("items");
		for (int i = 0; i < array.size(); i++ ) {
			builder.addItems(array.getInt(i));
		}
		
		sendMessageTo(clientID, Protocol.MessageType.RESPONSE_GET_ITEMS_VALUE, builder.build().toByteArray());
	}
	
	private void handleRequestUpdateAction(int clientID, Protocol.RequestUpdateAction request) {
		try {
			int userID = request.getUserID();
			User user = UserManager.getInstance().getIdentifiedUser(userID);
			if(user == null)
				return;
			int mapID = user.getPosition().getMapID();

			List<Integer> userList = MapManager.getInstance().getUserList(mapID);

			for (Integer id : userList) {
				User sendUser = UserManager.getInstance().getIdentifiedUser(id.intValue());
				if (id.intValue() != userID) {
					sendMessageTo(sendUser.getConnectionID(), Protocol.MessageType.RESPONSE_UPDATE_ACTION_VALUE,
							request.toByteArray());
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage() + "handleRequestUpdateAction userID : " + request.getUserID());
		}
	}
	
	private void handleRequestGetPrototype(int clientID, Protocol.RequestGetPrototype request) {
		Protocol.ResponseGetPrototype.Builder builder = Protocol.ResponseGetPrototype.newBuilder();
		JsonFormat.Parser parser = JsonFormat.parser();

		JsonObject items = DataManager.getInstance().get("Prototype_Items");
		JsonArray useItems = items.getArray("use");
		try {

			for (int i = 0; i < useItems.size(); i++) {
				String json = useItems.getObject(i).toString();
				Protocol.Use.Builder newBuilder = Protocol.Use.newBuilder();
				parser.merge(json, newBuilder);

				builder.addItems(Protocol.Item.newBuilder()
						.setType(Protocol.ItemType.ITEM_TYPE_USE)
						.setData(newBuilder.build().toByteString())
						.build());
			}

			JsonArray collectItems = items.getArray("collect");
			for (int i = 0; i < collectItems.size(); i++) {
				String json = collectItems.getObject(i).toString();
				Protocol.Collect.Builder newBuilder = Protocol.Collect.newBuilder();
				parser.merge(json, newBuilder);

				builder.addItems(Protocol.Item.newBuilder()
						.setType(Protocol.ItemType.ITEM_TYPE_COLLECT)
						.setData(newBuilder.build().toByteString())
						.build());
			}

			JsonArray equipItems = items.getArray("equip");
			for (int i = 0; i < equipItems.size(); i++) {
				String json = equipItems.getObject(i).toString();
				Protocol.Equip.Builder newBuilder = Protocol.Equip.newBuilder();
				parser.merge(json, newBuilder);

				builder.addItems(Protocol.Item.newBuilder()
						.setType(Protocol.ItemType.ITEM_TYPE_EQUIP)
						.setData(newBuilder.build().toByteString())
						.build());
			}

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		sendMessageTo(clientID, Protocol.MessageType.REQUEST_GET_PROTOTYPE_VALUE, builder.build().toByteArray());
	}
}
