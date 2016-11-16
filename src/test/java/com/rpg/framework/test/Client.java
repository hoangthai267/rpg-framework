package com.rpg.framework.test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.rpg.framework.client.SocketClient;
import com.rpg.framework.database.Protocol;
import com.rpg.framework.database.Protocol.Item;

public class Client extends SocketClient {
	private enum State {
		SEND_REQUEST_LOGIN, 
		SEND_REQUEST_REGISTER, 
		SEND_REQUEST_GET_CHARACTER, 
		SEND_REQUEST_CREATE_CHARACTER, 
		SEND_REQUEST_START_GAME, 
		SEND_REQUEST_UPDATE_POSITION,
		SEND_REQUEST_GET_ITEMS,
		SEND_REQUEST_UPDATE_ACTION,
		
		WAIT_RESPONSE, 
		HANDLE_RESPONSE, 
		
		IDLE, 
		START, 
		STOP,
	}

	private String host;
	private int port;
	private State state;

	private String userName;
	private String password;
	private int userID;
	private Protocol.Character character;
	private int mapID;
	private double positionX;
	private double positionY;
	private int commandID;
	private byte[] data;
	private Random rand;
	private List<Integer> items;
	private float updatedTime;
	private Map<Integer, byte[]> messageList;
	private boolean running;
	public Client(String host, int port) {
		super(host, port);
		this.host = host;
		this.port = port;
		this.state = State.IDLE;
		this.rand = new Random();
		this.running = true;
		messageList = new HashMap();
	}

	public void start() {
		super.start();
		System.out.println("Client start with " + userName);
		this.state = State.SEND_REQUEST_LOGIN;
	}

	public void start(String userName, String password) {
		this.userName = userName;
		this.password = password;
		this.state = State.START;
		this.loop();
	}
	
	public void loop() {
		long lastLoopTime = System.nanoTime();
		final int TARGET_FPS = 60;
		final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
		long lastFpsTime = 0;
		int fps = 0;
		// keep looping round til the game ends
		while (running) {
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
//				System.out.println("(FPS: " + fps + ")");
				lastFpsTime = 0;
				fps = 0;
			}

			// update the game logic
			update(delta);

			// draw everyting
			// render();
		}
	}

	public void stop() {
		super.stop();
		this.running = false;
	}

	public void update(double delta) {
//		System.out.println(state);
		if(updatedTime >= 1.0f) {
//			requestUpdateAction();
//			requestUpdatePosition();
			updatedTime -= 1.0f;			
		} else {
			updatedTime += delta;			

		}
		
		switch (state) {
		case IDLE: {
			break;
		}
		case SEND_REQUEST_LOGIN: {
			requestLogin();
			break;
		}
		case SEND_REQUEST_REGISTER: {
			requestRegister();
			break;
		}
		case SEND_REQUEST_GET_CHARACTER: {
			requestGetCharacter();
			break;
		}
		case SEND_REQUEST_CREATE_CHARACTER: {
			requestCreateCharacter();
			break;
		}
		case SEND_REQUEST_START_GAME: {
			requestStartGame();
			break;
		}
		case SEND_REQUEST_UPDATE_POSITION: {
			//requestUpdatePosition();
			break;
		}
		case SEND_REQUEST_GET_ITEMS: {
			requestGetItems();
			break;
		}
		case SEND_REQUEST_UPDATE_ACTION: {
			requestUpdateAction();
			break;
		}
		case WAIT_RESPONSE: {
			break;
		}
		case START: {
			start();
//			requestGetPrototype();
			break;
		}
		case STOP: {
			stop();
			break;
		}
		case HANDLE_RESPONSE: {
			handleMessage();
			break;
		}
		default:
			break;
		}
	}

	public void send(int commandID, byte[] data) {
		super.send(commandID, data);
	}

	public void receive(int commandID, byte[] data) {
		super.receive(commandID, data);
		this.state = State.HANDLE_RESPONSE;
		this.messageList.put(commandID, data);
	}

	public void handleMessage() {
		for(Map.Entry<Integer, byte[]> entry : messageList.entrySet()) {
			int commandID = entry.getKey();
			byte[] data = entry.getValue();
			handleMessage(commandID, data);
		}
		
		messageList.clear();
	}
	
	public void handleMessage(int commandID, byte[] data) {
		try {
			switch (commandID) {
			case Protocol.MessageType.RESPONE_LOGIN_VALUE: {
				responseLogin(Protocol.ResponseLogin.parseFrom(data));
				break;
			}
			case Protocol.MessageType.RESPONE_REGISTER_VALUE: {
				responseRegister(Protocol.ResponseRegister.parseFrom(data));
				break;
			}
			case Protocol.MessageType.RESPONE_GET_CHARACTER_VALUE: {
				responseGetCharacter(Protocol.ResponseGetCharacter.parseFrom(data));
				break;
			}
			case Protocol.MessageType.RESPONE_CREATE_CHARACTER_VALUE: {
				responseCreateCharacter(Protocol.ResponseCreateCharacter.parseFrom(data));
				break;
			}
			case Protocol.MessageType.RESPONE_START_GAME_VALUE: {
				responseStartGame(Protocol.ResponseStartGame.parseFrom(data));
				break;
			}
			case Protocol.MessageType.RESPONE_UPDATE_POSITION_VALUE: {
				responseUpdatePosition(Protocol.ResponseUpdatePosition.parseFrom(data));
				break;
			}
			case Protocol.MessageType.RESPONSE_GET_ITEMS_VALUE: {
				responseGetItems(Protocol.ResponseGetItems.parseFrom(data));
				break;
			}
			case Protocol.MessageType.RESPONSE_UPDATE_ACTION_VALUE: {
				System.out.println(Protocol.ResponseUpdateAction.parseFrom(data).getUserID());
				break;
			}
			case Protocol.MessageType.RESPONSE_GET_PROTOTYPE_VALUE: {
				responseGetPrototype(Protocol.ResponseGetPrototype.parseFrom(data));
			}
			case Protocol.MessageType.MESSAGE_KILL_MONSTER_VALUE: {
				System.out.println("MESSAGE_KILL_MONSTER_VALUE: " + Protocol.MessageKillMonster.parseFrom(data).getMonsterIndex());
				break;
			}
			
			case Protocol.MessageType.MESSAGE_RESPAWN_MONSTER_VALUE: {
				System.out.println("MESSAGE_RESPAWN_MONSTER_VALUE: " + Protocol.MessageRespawnMonster.parseFrom(data).getMonsterIndex());
				break;
			}	
			
			default: {
				state = State.STOP;
				break;
			}
			}

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	public void requestLogin() {
		Protocol.RequestLogin.Builder builder = Protocol.RequestLogin.newBuilder();
		builder.setUsername(userName);
		builder.setPassword(password);

		this.state = State.WAIT_RESPONSE;
		send(Protocol.MessageType.REQUEST_LOGIN_VALUE, builder.build().toByteArray());

	}

	public void requestRegister() {
		Protocol.RequestRegister.Builder builder = Protocol.RequestRegister.newBuilder();
		builder.setUsername(userName);
		builder.setPassword(password);

		this.state = State.WAIT_RESPONSE;
		send(Protocol.MessageType.REQUEST_REGISTER_VALUE, builder.build().toByteArray());

	}

	public void requestGetCharacter() {
		Protocol.RequestGetCharacter.Builder builder = Protocol.RequestGetCharacter.newBuilder();
		builder.setUserID(userID);

		send(Protocol.MessageType.REQUEST_GET_CHARACTER_VALUE, builder.build().toByteArray());
		this.state = State.WAIT_RESPONSE;
	}

	public void requestCreateCharacter() {
		Protocol.RequestCreateCharacter.Builder builder = Protocol.RequestCreateCharacter.newBuilder();
		builder.setUserID(userID);
		builder.setName(userName);
		builder.setGender(0);

		send(Protocol.MessageType.REQUEST_CREATE_CHARACTER_VALUE, builder.build().toByteArray());

		this.state = State.WAIT_RESPONSE;
	}

	public void requestStartGame() {
		Protocol.RequestStartGame.Builder builder = Protocol.RequestStartGame.newBuilder();
		builder.setUserID(userID);

		send(Protocol.MessageType.REQUEST_START_GAME_VALUE, builder.build().toByteArray());

		this.state = State.WAIT_RESPONSE;
	}

	public void requestUpdatePosition() {	
		Protocol.RequestUpdatePosition request = Protocol.RequestUpdatePosition.newBuilder().setUserID(userID)
				.setMapID(mapID).setX(positionX).setY(positionY).build();

		send(Protocol.MessageType.REQUEST_UPDATE_POSITION_VALUE, request.toByteArray());
	}

	public void requestGetItems() {
		Protocol.RequestGetItems request = Protocol.RequestGetItems.newBuilder()
				.setUserID(userID).build();
		
		send(Protocol.MessageType.REQUEST_GET_ITEMS_VALUE, request.toByteArray());
	}
	
	public void requestUpdateAction() {
		Protocol.RequestUpdateAction.Builder builder = Protocol.RequestUpdateAction.newBuilder();
		builder.setUserID(userID);
		builder.addActions(Protocol.CharacterAction.newBuilder()
				.setState(10)
				.setActionCommand(100)
				.setTimeStart(10)
				.setTimeEnd(100).build());
		builder.addActions(Protocol.CharacterAction.newBuilder()
				.setState(20)
				.setActionCommand(200)
				.setTimeStart(100)
				.setTimeEnd(200).build());
		send(Protocol.MessageType.REQUEST_UPDATE_ACTION_VALUE, builder.build().toByteArray());
	}
	
	public void requestGetPrototype() {
		Protocol.RequestGetPrototype request = Protocol.RequestGetPrototype.newBuilder()
				.build();
		
		send(Protocol.MessageType.REQUEST_GET_PROTOTYPE_VALUE, request.toByteArray());		
	}
	
	public void responseLogin(Protocol.ResponseLogin response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {
			this.userID = response.getUserID();
			System.out.println("Login success " + userID);
			if (response.getHasCharacter()) {
				this.state = State.SEND_REQUEST_GET_CHARACTER;
			} else {
				this.state = State.SEND_REQUEST_CREATE_CHARACTER;
			}
		} else {
			this.state = State.SEND_REQUEST_REGISTER;
		}
	}

	public void responseRegister(Protocol.ResponseRegister response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {
			System.out.println("Register success");
			this.state = State.SEND_REQUEST_LOGIN;
		} else {
			this.state = State.STOP;
		}
	}

	public void responseGetCharacter(Protocol.ResponseGetCharacter response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {
			this.character = response.getCharacter();
			this.state = State.SEND_REQUEST_START_GAME;
		} else {

		}
	}

	public void responseCreateCharacter(Protocol.ResponseCreateCharacter response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {
			this.state = State.SEND_REQUEST_GET_CHARACTER;
		}
	}

	public void responseStartGame(Protocol.ResponseStartGame response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {
			this.state = State.SEND_REQUEST_UPDATE_POSITION;
			this.mapID = 0;
			System.out.println(response.getMonstersList());
		}
	}

	public void responseUpdatePosition(Protocol.ResponseUpdatePosition response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {

			positionX = rand.nextDouble() * 100;
			positionY = rand.nextDouble() * 100;
		}
	}

	public void responseGetItems(Protocol.ResponseGetItems response) {
		if(response.getResult() == Protocol.ResponseCode.SUCCESS) {
			this.items = response.getItemsList();
			System.out.println(items);
		}
	}
	
	public void responseGetPrototype(Protocol.ResponseGetPrototype response) throws InvalidProtocolBufferException {
		List<Protocol.Item> items = response.getItemsList();

		for (Protocol.Item item : items) {
			switch (item.getType().getNumber()) {
			case Protocol.ItemType.ITEM_TYPE_USE_VALUE: {
				System.out.println(Protocol.Use.parseFrom(item.getData()));
				break;
			}

			case Protocol.ItemType.ITEM_TYPE_COLLECT_VALUE: {
				System.out.println(Protocol.Collect.parseFrom(item.getData()));
				break;
			}

			case Protocol.ItemType.ITEM_TYPE_EQUIP_VALUE: {
				System.out.println(Protocol.Equip.parseFrom(item.getData()));
				break;
			}

			default:
				break;
			}
		}
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
	
	public static void main(String args[]) {
//		for (int i = 1; i < 100; i++)
//			new Client("localhost", 8463).start("admin" + i, "admin");
//		new Client("localhost", 8463).start("admin", "admin");
//		new Client("localhost", 8463).start("admin1", "admin");
//		new Client("localhost", 8463).start("admin2", "admin");
		new Client("localhost", 8463).start(args[0], "admin");
	}
}
