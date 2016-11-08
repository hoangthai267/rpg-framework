package com.rpg.framework.test;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rpg.framework.client.SocketClient;
import com.rpg.framework.database.Protocol;

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
	private Timer gameloop;
	private State state;

	private String userName;
	private String password;
	private String userID;
	private Protocol.Character character;
	private String mapID;
	private double positionX;
	private double positionY;
	private int commandID;
	private byte[] data;
	private Random rand;
	private List<Integer> items;
	
	public Client(String host, int port) {
		super(host, port);
		this.host = host;
		this.port = port;
		this.state = State.IDLE;
		this.gameloop = new Timer();
		this.rand = new Random();
	}

	public void start() {
		super.start();
		this.state = State.SEND_REQUEST_LOGIN;
	}

	public void start(String userName, String password) {
		this.userName = userName;
		this.password = password;
		this.state = State.START;
		this.gameloop.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				update();
				// render();
			}
		}, 0, 1000 / 30);
	}

	public void stop() {
		super.stop();
		this.gameloop.cancel();
	}

	public void update() {
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
			requestUpdatePosition();
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
			break;
		}
		case STOP: {
			stop();
			break;
		}
		case HANDLE_RESPONSE: {
			handleMessage(commandID, data);
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
		this.commandID = commandID;
		this.data = data;
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
				System.out.println(Protocol.RequestUpdateAction.parseFrom(data));
				this.state = State.STOP;
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
		this.state = State.WAIT_RESPONSE;

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
		this.state = State.WAIT_RESPONSE;
		
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
	
	public void responseLogin(Protocol.ResponseLogin response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {
			this.userID = response.getUserID();
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
			this.mapID = "Map_1";
		}
	}

	public void responseUpdatePosition(Protocol.ResponseUpdatePosition response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {

			positionX = rand.nextDouble() * 100;
			positionY = rand.nextDouble() * 100;

			this.state = State.SEND_REQUEST_UPDATE_ACTION;
		}
	}

	public void responseGetItems(Protocol.ResponseGetItems response) {
		if(response.getResult() == Protocol.ResponseCode.SUCCESS) {
			this.items = response.getItemsList();
			System.out.println(items);
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
		new Client("localhost", 8463).start("admin", "admin");
//		new Client("localhost", 8463).start("admin2", "admin");
	}
}
