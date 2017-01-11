package com.rpg.framework.test;

import java.util.List;
import java.util.Queue;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.rpg.framework.core.Debugger;
import com.rpg.framework.database.Protocol;
import com.rpg.framework.database.Protocol.MessageUpdateMonsterState;
import com.rpg.framework.entity.Message;
import com.rpg.framework.entity.Monster;
import com.rpg.framework.entity.Quest;
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
		Debugger.Write("Connected client: " + clientID);
		UserManager.getInstance().addAnonymousUser(clientID);
	}

	@Override
	public void disconnectedClient(int clientID) {
		Debugger.Write("Disconnected client: " + clientID);
		if (!UserManager.getInstance().removeIdentifiedUser(clientID))
			UserManager.getInstance().removeAnonymousUser(clientID);
	}

	@Override
	public void receiveMessageFrom(int clientID, int messageID, byte[] data) {
		MessageManager.getInstance().receiveMessage(clientID, messageID, data);
	}

	@Override
	public void updateSecond(double delta, int fps) {
		super.updateSecond(delta, fps);
		Debugger.Write("FPS : " + fps + " ");
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

		try {

			Queue<Message> messages = MessageManager.getInstance().getMessages();
			while (!messages.isEmpty()) {
				// for (Message message : messages) {
				// for(int i = 0; i < messages.size(); i++) {
				// Message message = messages.get(i);
				Message message = messages.poll();
				if (message == null)
					continue;
				switch (message.getType()) {
				// send to one
				case 1: {
					sendMessageTo(message.getChannelID(), message.getCommandID(), message.getData());
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
		} catch (Exception ex) {
			Debugger.WriteException(ex);
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
			case Protocol.MessageType.MESSAGE_UPDATE_MONSTER_STATE_VALUE: {
				handleMessageUpdateMonsterState(Protocol.MessageUpdateMonsterState.parseFrom(data));
				break;
			}

			case Protocol.MessageType.REQUEST_CHANGE_MAP_VALUE: {
				handleRequestChangeMap(clientID, Protocol.RequestChangeMap.parseFrom(data));
				break;
			}

			case Protocol.MessageType.MESSAGE_UPDATE_USER_COLLISION_VALUE: {
				handleMessageUpdateUserCollision(Protocol.MessageUpdateUserCollision.parseFrom(data));
				break;
			}

			case Protocol.MessageType.MESSAGE_UPDATE_MONSTER_COLLISION_VALUE: {
				handleMessageUpdateMonsterCollision(Protocol.MessageUpdateMonsterCollision.parseFrom(data));
				break;
			}

			case Protocol.MessageType.MESSAGE_BEGIN_QUEST_VALUE: {
				handleMessageBeginQuest(Protocol.MessageBeginQuest.parseFrom(data));
				break;
			}

			case Protocol.MessageType.MESSAGE_UPDATE_QUEST_VALUE: {
				handleMessageUpdateQuest(Protocol.MessageUpdateQuest.parseFrom(data));
				break;
			}

			case Protocol.MessageType.MESSAGE_END_QUEST_VALUE: {
				handleMessageEndQuest(Protocol.MessageEndQuest.parseFrom(data));
				break;
			}

			default: {
				break;
			}
			}

		} catch (InvalidProtocolBufferException e) {
			Debugger.WriteException(e);
		}
	}

	private void handleRequestLogin(int clientID, Protocol.RequestLogin request) {
		try {
			Protocol.ResponseLogin.Builder builder = Protocol.ResponseLogin.newBuilder();

			JsonObject accounts = DataManager.getInstance().get("Accounts");
			if (accounts.containsKey(request.getUsername())) {
				JsonObject user = accounts.getObject(request.getUsername());
				String userName = user.getString("username");
				String password = user.getString("password");
				int userID = user.getInt("userID");

				if (password.compareTo(request.getPassword()) == 0) {
					user = DataManager.getInstance().get("User_" + userID);

					boolean hasLogin = user.getBoolean("hasLogin");
					boolean hasCharacter = user.getBoolean("hasCharacter");

					if (hasLogin) {
						builder.setResult(Protocol.ResponseCode.FAIL);
						builder.setMessage("The account has been logged.");
						builder.setUserID(userID);
						builder.setHasCharacter(hasCharacter);
					} else {
						builder.setResult(Protocol.ResponseCode.SUCCESS);
						builder.setMessage("Sucess.");
						builder.setUserID(userID);
						builder.setHasCharacter(hasCharacter);

						if (hasCharacter) {
							System.out.println("Server.handleRequestLogin(): " + userName);
							UserManager.getInstance().addIdentifiedUser(clientID, userID);
							user.put("hasLogin", true);
							DataManager.getInstance().set("User_" + userID, user);
							builder.setMapID(DataManager.getInstance().get("User_" + request.getUserID() + "_Position")
									.getInt("mapID"));
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
		} catch (Exception ex) {
			Debugger.WriteException(ex);
		}
	}

	private void handleRequestRegister(int clientID, Protocol.RequestRegister request) {
		try {
			Protocol.ResponseRegister.Builder builder = Protocol.ResponseRegister.newBuilder();

			JsonObject accounts = DataManager.getInstance().get("Accounts");
			if (accounts.getObject(request.getUsername()) != null) {
				builder.setResult(Protocol.ResponseCode.FAIL);
				builder.setMessage("Invalid username");
			} else {
				long count = accounts.getInt("total");

				JsonObject user = JsonObject.create().put("username", request.getUsername())
						.put("password", request.getPassword()).put("userID", count).put("hasCharacter", false)
						.put("hasLogin", false);

				DataManager.getInstance().set("User_" + count, user);

				accounts.put(request.getUsername(), user.removeKey("hasCharacter").removeKey("hasLogin"));
				accounts.put("total", count + 1);
				DataManager.getInstance().set("Accounts", accounts);

				builder.setResult(Protocol.ResponseCode.SUCCESS);
				builder.setMessage("Success");
			}
			sendMessageTo(clientID, Protocol.MessageType.RESPONE_REGISTER_VALUE, builder.build().toByteArray());
		} catch (Exception ex) {
			Debugger.WriteException(ex);
		}
	}

	private void handleRequestGetCharacter(int clientID, Protocol.RequestGetCharacter request) {
		try {
			JsonObject stats = DataManager.getInstance().get("User_" + request.getUserID() + "_Stats");
			JsonObject position = DataManager.getInstance().get("User_" + request.getUserID() + "_Position");
			JsonObject status = DataManager.getInstance().get("User_" + request.getUserID() + "_Status");
			JsonObject quest = DataManager.getInstance().get("User_" + request.getUserID() + "_Quests");
			Protocol.ResponseGetCharacter.Builder builder = Protocol.ResponseGetCharacter.newBuilder()
					.setName(stats.getString("name")).setGender(stats.getInt("gender"))
					.setOccupation(stats.getInt("occupation")).setLevel(stats.getInt("level"))
					.setStrength(stats.getInt("strength")).setMagic(stats.getInt("magic"))
					.setDefense(stats.getInt("defense")).setSpeed(stats.getInt("speed")).setDame(stats.getInt("dame"))
					.setArmor(stats.getInt("armor"))

					.setMapID(position.getInt("mapID")).setX(position.getDouble("x")).setY(position.getDouble("y"))

					.setMaxHP(status.getInt("maxHP")).setCurHP(status.getInt("curHP")).setMaxMP(status.getInt("maxMP"))
					.setCurMP(status.getInt("curMP")).setMaxEXP(status.getInt("maxEXP"))
					.setCurEXP(status.getInt("curEXP"));

			JsonArray list = quest.getArray("list");

			for (int i = 0; i < list.size(); i++) {
				JsonObject obj = quest.getObject(list.getString(i));
				Protocol.Quest.Builder questBuilder = Protocol.Quest.newBuilder().setID(obj.getInt("ID"))
						.setStep(obj.getInt("Step")).setState(obj.getInt("State"));

				JsonArray progressList = obj.getArray("Progress");
				for (Object object : progressList) {
					questBuilder.addProgress((Integer) object);
				}

				builder.addQuest(questBuilder);
			}

			sendMessageTo(clientID, Protocol.MessageType.RESPONE_GET_CHARACTER_VALUE, builder.build().toByteArray());
		} catch (Exception ex) {
			Debugger.WriteException(ex);
		}
	}

	private void handleRequestCreateCharacter(int clientID, Protocol.RequestCreateCharacter request) {
		try {
			Protocol.ResponseCreateCharacter.Builder builder = Protocol.ResponseCreateCharacter.newBuilder();
			builder.setResult(Protocol.ResponseCode.SUCCESS);

			JsonObject user = DataManager.getInstance().get("User_" + request.getUserID());
			if (!user.getBoolean("hasCharacter")) {
				user.put("hasCharacter", true);
				DataManager.getInstance().set("User_" + request.getUserID(), user);

				JsonObject stats = JsonObject.create().put("name", request.getName()).put("gender", 0)
						.put("occupation", request.getOccupation()).put("level", 1).put("strength", 1).put("magic", 1)
						.put("defense", Config.CHARACTER_DEFENSE).put("speed", Config.CHARACTER_SPEED)
						.put("dame", Config.CHARACTER_DAMAGE).put("armor", 1);

				JsonObject position = JsonObject.create().put("mapID", 1).put("x", 0.0).put("y", 0.0);

				JsonObject status = JsonObject.create().put("maxHP", Config.CHARACTER_HP)
						.put("curHP", Config.CHARACTER_HP).put("maxMP", Config.CHARACTER_MP)
						.put("curMP", Config.CHARACTER_MP).put("curEXP", 0).put("maxEXP", Config.CHARACTER_EXP);

				JsonObject items = JsonObject.create().put("items", JsonArray.create().add(0).add(1));

				// JsonObject character = JsonObject.create().put("stats",
				// stats).put("position", position).put("status", status);

				DataManager.getInstance().set("User_" + request.getUserID() + "_Stats", stats);
				DataManager.getInstance().set("User_" + request.getUserID() + "_Position", position);
				DataManager.getInstance().set("User_" + request.getUserID() + "_Status", status);
				DataManager.getInstance().set("User_" + request.getUserID() + "_Items", items);
				DataManager.getInstance().set("User_" + request.getUserID() + "_Quests",
						JsonObject.create().put("list", JsonArray.create()));
				// couchbase.set(request.getUserID() + "_Character", character);
				UserManager.getInstance().addIdentifiedUser(clientID, request.getUserID());
			}
			sendMessageTo(clientID, Protocol.MessageType.RESPONE_CREATE_CHARACTER_VALUE, builder.build().toByteArray());
		} catch (Exception ex) {
			Debugger.WriteException(ex);
		}
	}

	private void handleRequestStartGame(int clientID, Protocol.RequestStartGame request) {
		try {
			Protocol.ResponseStartGame.Builder builder = Protocol.ResponseStartGame.newBuilder();

			int userID = request.getUserID();
			int mapID = UserManager.getInstance().getIdentifiedUser(userID).getMapID();

			List<Integer> userList = MapManager.getInstance().getUserList(mapID);
			List<Integer> monsterList = MapManager.getInstance().getMonsterList(mapID);
			// List<Integer> itemList =
			// MapManager.getInstance().getItemList(mapID);

			for (Integer id : userList) {
				User user = UserManager.getInstance().getIdentifiedUser(id);

				builder.addUsers(
						Protocol.User.newBuilder().setId(id).setOccupation(user.getOccupation()).setName(user.getName())

								.setMapID(user.getMapID()).setX(user.getPositionX()).setY(user.getPositionY())

								.setMaxHP(user.getMaxHP()).setCurHP(user.getCurHP()).setMaxMP(user.getMaxMP())
								.setCurMP(user.getCurMP())

								.setDamage(user.getDamage()).setDefense(user.getDefense()).setSpeed(user.getSpeed()));
			}

			for (int i = 0; i < monsterList.size(); i++) {
				Monster entity = MonsterManager.getInstance().getMonsterInList(monsterList.get(i));

				builder.addMonsters(Protocol.Monster.newBuilder().setId(entity.getId()).setIndex(entity.getIndex())
						.setMapID(entity.getMapID()).setX(entity.getPositionX()).setY(entity.getPositionY())

						.setDamage(entity.getDamage()).setDefense(entity.getDefense()).setSpeed(entity.getSpeed())

						.setCurHP(entity.getCurHP()).setCurMP(entity.getCurMP()).setMaxHP(entity.getMaxHP())
						.setMaxMP(entity.getMaxMP()));
			}
			MapManager.getInstance().enterMap(request.getUserID(), mapID);

			sendMessageTo(clientID, Protocol.MessageType.RESPONE_START_GAME_VALUE, builder.build().toByteArray());
		} catch (Exception ex) {
			Debugger.WriteException(ex);
		}
	}

	private void handleRequestUpdatePosition(int clientID, Protocol.RequestUpdatePosition request) {
		try {
			String id = "User_" + request.getUserID() + "_Position";
			JsonObject characterPosition = JsonObject.create().put("mapID", request.getMapID()).put("x", request.getX())
					.put("y", request.getY());

			User user = UserManager.getInstance().getIdentifiedUser(request.getUserID());

			if (user == null)
				return;

			user.setPosition(request.getMapID(), request.getX(), request.getY());

			DataManager.getInstance().cached(id, characterPosition);

			Protocol.ResponseUpdatePosition.Builder builder = Protocol.ResponseUpdatePosition.newBuilder();
			builder.setResult(Protocol.ResponseCode.SUCCESS);

			// sendMessageTo(clientID,
			// Protocol.MessageType.RESPONE_UPDATE_POSITION_VALUE,
			// builder.build().toByteArray());
		} catch (Exception ex) {
			Debugger.WriteException(ex);
		}
	}

	private void handleRequestGetItem(int clientID, Protocol.RequestGetItems request) {
		try {
			Protocol.ResponseGetItems.Builder builder = Protocol.ResponseGetItems.newBuilder();

			builder.setResult(Protocol.ResponseCode.SUCCESS);

			JsonArray array = DataManager.getInstance().get("User_" + request.getUserID() + "_Items").getArray("items");
			for (int i = 0; i < array.size(); i++) {
				builder.addItems(array.getInt(i));
			}

			sendMessageTo(clientID, Protocol.MessageType.RESPONSE_GET_ITEMS_VALUE, builder.build().toByteArray());
		} catch (Exception ex) {
			Debugger.WriteException(ex);
		}
	}

	private void handleRequestUpdateAction(int clientID, Protocol.RequestUpdateAction request) {
		try {
			int userID = request.getUserID();
			User user = UserManager.getInstance().getIdentifiedUser(userID);
			if (user == null)
				return;
			int mapID = user.getMapID();

			List<Integer> userList = MapManager.getInstance().getUserList(mapID);

			for (Integer id : userList) {
				User sendUser = UserManager.getInstance().getIdentifiedUser(id.intValue());
				if (id.intValue() != userID) {
					sendMessageTo(sendUser.getConnectionID(), Protocol.MessageType.RESPONSE_UPDATE_ACTION_VALUE,
							request.toByteArray());
				}
			}
		} catch (Exception ex) {
			Debugger.WriteException(ex);
		}
	}

	private void handleRequestGetPrototype(int clientID, Protocol.RequestGetPrototype request) {
		try {

			Protocol.ResponseGetPrototype.Builder builder = Protocol.ResponseGetPrototype.newBuilder();
			JsonFormat.Parser parser = JsonFormat.parser();

			JsonObject items = DataManager.getInstance().get("Prototype_Items");
			JsonArray useItems = items.getArray("use");

			for (int i = 0; i < useItems.size(); i++) {
				String json = useItems.getObject(i).toString();
				Protocol.Use.Builder newBuilder = Protocol.Use.newBuilder();
				parser.merge(json, newBuilder);

				builder.addItems(Protocol.Item.newBuilder().setType(Protocol.ItemType.ITEM_TYPE_USE)
						.setData(newBuilder.build().toByteString()).build());
			}

			JsonArray collectItems = items.getArray("collect");
			for (int i = 0; i < collectItems.size(); i++) {
				String json = collectItems.getObject(i).toString();
				Protocol.Collect.Builder newBuilder = Protocol.Collect.newBuilder();
				parser.merge(json, newBuilder);

				builder.addItems(Protocol.Item.newBuilder().setType(Protocol.ItemType.ITEM_TYPE_COLLECT)
						.setData(newBuilder.build().toByteString()).build());
			}

			JsonArray equipItems = items.getArray("equip");
			for (int i = 0; i < equipItems.size(); i++) {
				String json = equipItems.getObject(i).toString();
				Protocol.Equip.Builder newBuilder = Protocol.Equip.newBuilder();
				parser.merge(json, newBuilder);

				builder.addItems(Protocol.Item.newBuilder().setType(Protocol.ItemType.ITEM_TYPE_EQUIP)
						.setData(newBuilder.build().toByteString()).build());
			}

			sendMessageTo(clientID, Protocol.MessageType.REQUEST_GET_PROTOTYPE_VALUE, builder.build().toByteArray());

		} catch (Exception ex) {
			Debugger.WriteException(ex);
		}
	}

	private void handleMessageUpdateMonsterState(Protocol.MessageUpdateMonsterState message) {
		try {

			MapManager.getInstance().sendMessageUpdateMonsterState(message.getMapID(), message.toByteArray());

			for (Protocol.MonsterState data : message.getDataList()) {
				Monster entity = MonsterManager.getInstance().getMonsterInList(data.getIndex());

				entity.setPosition(message.getMapID(), data.getPositionX(), data.getPositionY());
				entity.setState(data.getState());
				entity.setDirection(data.getDirection());
			}

		} catch (Exception ex) {
			Debugger.WriteException(ex);
		}
	}

	public void handleRequestChangeMap(int clientID, Protocol.RequestChangeMap request) {
		try {
			Protocol.ResponseChangeMap.Builder builder = Protocol.ResponseChangeMap.newBuilder();

			int userID = request.getUserID();
			int mapID = request.getTo();

			List<Integer> userList = MapManager.getInstance().getUserList(mapID);
			List<Integer> monsterList = MapManager.getInstance().getMonsterList(mapID);
			// List<Integer> itemList =
			// MapManager.getInstance().getItemList(mapID);

			for (Integer id : userList) {
				User user = UserManager.getInstance().getIdentifiedUser(id);

				builder.addUsers(
						Protocol.User.newBuilder().setId(id).setOccupation(user.getOccupation()).setName(user.getName())

								.setMapID(user.getMapID()).setX(user.getPositionX()).setY(user.getPositionY())

								.setMaxHP(user.getMaxHP()).setCurHP(user.getCurHP()).setMaxMP(user.getMaxMP())
								.setCurMP(user.getCurMP())

								.setDamage(user.getDamage()).setDefense(user.getDefense()).setSpeed(user.getSpeed()));
			}

			for (int i = 0; i < monsterList.size(); i++) {
				Monster entity = MonsterManager.getInstance().getMonsterInList(monsterList.get(i));

				builder.addMonsters(Protocol.Monster.newBuilder().setId(entity.getId()).setIndex(entity.getIndex())

						.setMapID(entity.getMapID()).setX(entity.getPositionX()).setY(entity.getPositionY())

						.setDamage(entity.getDamage()).setDefense(entity.getDefense()).setSpeed(entity.getSpeed())

						.setCurHP(entity.getCurHP()).setCurMP(entity.getCurMP()).setMaxHP(entity.getMaxHP())
						.setMaxMP(entity.getMaxMP()));
			}
			MapManager.getInstance().changeMap(request.getUserID(), request.getFrom(), request.getTo());

			builder.setMapID(request.getTo());

			sendMessageTo(clientID, Protocol.MessageType.RESPONSE_CHANGE_MAP_VALUE, builder.build().toByteArray());

		} catch (Exception ex) {
			Debugger.WriteException(ex);
		}
	}

	public void handleMessageUpdateUserCollision(Protocol.MessageUpdateUserCollision message) {
		try {

			User user = UserManager.getInstance().getIdentifiedUser(message.getUserID());
			Monster monster = MonsterManager.getInstance().getMonsterInList(message.getIndex());
			if (monster == null)
				return;
			int subHP = user.getCurHP() - monster.getDamage();
			System.out.println(subHP);
			user.setCurHP(subHP);
			user.saveData();

			List<Integer> list = MapManager.getInstance().getUserList(user.getMapID());
			for (Integer id : list) {
				sendMessageTo(UserManager.getInstance().getIdentifiedUser(id).getConnectionID(),
						Protocol.MessageType.MESSAGE_UPDATE_USER_COLLISION_VALUE, message.toByteArray());
			}

		} catch (Exception ex) {
			Debugger.WriteException(ex);
		}
	}

	public void handleMessageUpdateMonsterCollision(Protocol.MessageUpdateMonsterCollision message) {
		try {

			User user = UserManager.getInstance().getIdentifiedUser(message.getUserID());
			Monster monster = MonsterManager.getInstance().getMonsterInList(message.getIndex());
			if (monster == null)
				return;

			monster.attacked(user.getId(), user.getDamage());

			List<Integer> list = MapManager.getInstance().getUserList(user.getMapID());
			for (Integer id : list) {
				sendMessageTo(UserManager.getInstance().getIdentifiedUser(id).getConnectionID(),
						Protocol.MessageType.MESSAGE_UPDATE_MONSTER_COLLISION_VALUE, message.toByteArray());
			}

		} catch (Exception ex) {
			Debugger.WriteException(ex);
		}
	}

	public void handleMessageBeginQuest(Protocol.MessageBeginQuest message) {
		Quest quest = new Quest();
		quest.setID(message.getQuestID());
		quest.setStep(1);
		quest.setPercent(0.0);
		quest.setState(1);

		User user = UserManager.getInstance().getIdentifiedUser(message.getUserID());
		if (user.addQuest(message.getQuestID(), quest)) {

			JsonObject data = JsonObject.create();
			data.put("ID", quest.getID());
			data.put("Step", quest.getStep());
			data.put("Progress", JsonArray.create());
			data.put("State", 1); // in - proccess

			JsonObject list = DataManager.getInstance().get("User_" + message.getUserID() + "_Quests");
			list.put(String.valueOf(quest.getID()), data);
			list.getArray("list").add(String.valueOf(quest.getID()));
			DataManager.getInstance().set("User_" + message.getUserID() + "_Quests", list);
			
			MessageManager.getInstance().sendMessage(user.getConnectionID(), Protocol.MessageType.MESSAGE_INFORMATION_QUEST_VALUE, Protocol.MessageInformationQuest.newBuilder()
					.setID(quest.getID())
					.setStep(quest.getStep())
					.setState(quest.getState())
					.build().toByteArray());
		}
	}

	public void handleMessageUpdateQuest(Protocol.MessageUpdateQuest message) {
		User user = UserManager.getInstance().getIdentifiedUser(message.getUserID());
		Quest quest = user.getQuest(message.getQuestID());

		quest.setStep(message.getStep());
		quest.setProgress(message.getProgressList());

		JsonObject data = JsonObject.create();
		data.put("ID", quest.getID());
		data.put("Step", quest.getStep());
		data.put("Progress", JsonArray.from(message.getProgressList()));
		data.put("State", 1); // in - proccess

		JsonObject list = DataManager.getInstance().get("User_" + message.getUserID() + "_Quests");
		list.put(String.valueOf(quest.getID()), data);
		DataManager.getInstance().set("User_" + message.getUserID() + "_Quests", list);
	}

	public void handleMessageEndQuest(Protocol.MessageEndQuest message) {
		User user = UserManager.getInstance().getIdentifiedUser(message.getUserID());
		Quest quest = user.getQuest(message.getQuestID());

		quest.setPercent(100.0);
		quest.setState(2);

		JsonObject data = JsonObject.create();
		data.put("ID", quest.getID());
		data.put("Step", quest.getStep());
		data.put("Progress", JsonArray.create());
		data.put("State", 2); // completed

		JsonObject list = DataManager.getInstance().get("User_" + message.getUserID() + "_Quests");
		list.put(String.valueOf(quest.getID()), data);
		DataManager.getInstance().set("User_" + message.getUserID() + "_Quests", list);

		MessageManager.getInstance().sendMessage(user.getConnectionID(),
				Protocol.MessageType.MESSAGE_REWARDS_QUEST_VALUE, Protocol.MessageRewardsQuest.newBuilder()
						.setQuestID(message.getQuestID()).setBonusExp(100).build().toByteArray());
	}
}
