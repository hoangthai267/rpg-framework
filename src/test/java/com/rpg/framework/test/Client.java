package com.rpg.framework.test;

import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rpg.framework.database.Protocol;

public class Client extends com.rpg.framework.core.Client {
	
	private enum State {
		START,
		STOP,
		WAIT,
		RUN,
	}
	private State	state;
	
	private String 	userName;
	private String 	password;
	private int		userID;
	
	private int		mapID;
	private double	positionX;
	private double 	positionY;

	private List<Integer> items;
	
	public Client() {
		this.state 		= State.START;
		
		this.userName 	= "admin";
		this.password 	= "admin";
		this.userID 	= -1;
		
		this.mapID		= -1;
		this.positionX 	= 0.0;
		this.positionY 	= 0.0;
	}
	
	public Client(String userName, String password) {
		System.out.println(userName);
		this.state 		= State.START;
		
		this.userName 	= userName;
		this.password 	= password;
		this.userID		= -1;
		
		this.mapID		= -1;
		this.positionX 	= 0.0;
		this.positionY 	= 0.0;
	}
	
	@Override
	public boolean initialize() {		
		return super.initialize();
	}
	
	@Override
	public void receiveMessage(int commandID, byte[] data) {
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
					break;
				}
				case Protocol.MessageType.RESPONSE_GET_PROTOTYPE_VALUE: {
					responseGetPrototype(Protocol.ResponseGetPrototype.parseFrom(data));
					break;
				}
				case Protocol.MessageType.MESSAGE_KILL_MONSTER_VALUE: {
					break;
				}
				case Protocol.MessageType.MESSAGE_RESPAWN_MONSTER_VALUE: {
					break;
				}
				case Protocol.MessageType.MESSAGE_NEW_USER_VALUE: {
					break;
				}
				case Protocol.MessageType.MESSAGE_DELETE_USER_VALUE: {
					break;
				}
				default:
					break;
			}
			
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void sendMessage(int commandID, byte[] data) {
		super.sendMessage(commandID, data);
	}
	
	@Override
	public void start(String host, int port) {		
		super.start(host, port);
	}
	
	@Override
	public void stop() {		
		super.stop();
	}
	
	
	@Override
	public void update(double delta) {
		super.update(delta);
		
		switch (state) {
			case START: {			
				sendRequestLogin();
				sendRequestGetPrototype();
				break;
			}
			
			case RUN : {
				sendRequestUpdateAction();
				sendRequestUpdatePosition();
				break;
			}
			
			case STOP: {
				stop();
				break;
			}
			
			case WAIT: {
				break;
			}
	
			default: {
				break;
			}
		}
	}	
		
	public void sendRequestLogin() {
		System.out.println("Client.sendRequestLogin()");
		this.state = State.WAIT;
		
		Protocol.RequestLogin.Builder builder = Protocol.RequestLogin.newBuilder();
		builder.setUsername(userName);
		builder.setPassword(password);

		sendMessage(Protocol.MessageType.REQUEST_LOGIN_VALUE, builder.build().toByteArray());
	}
	
	public void sendRequestRegister() {
		System.out.println("Client.sendRequestRegister()");
		this.state = State.WAIT;
		Protocol.RequestRegister.Builder builder = Protocol.RequestRegister.newBuilder();
		builder.setUsername(userName);
		builder.setPassword(password);

		sendMessage(Protocol.MessageType.REQUEST_REGISTER_VALUE, builder.build().toByteArray());

	}

	public void sendRequestGetCharacter() {
		System.out.println("Client.sendRequestGetCharacter()");
		this.state = State.WAIT;
		Protocol.RequestGetCharacter.Builder builder = Protocol.RequestGetCharacter.newBuilder();
		builder.setUserID(userID);

		sendMessage(Protocol.MessageType.REQUEST_GET_CHARACTER_VALUE, builder.build().toByteArray());
	}

	public void sendRequestCreateCharacter() {
		System.out.println("Client.sendRequestCreateCharacter()");
		this.state = State.WAIT;
		Protocol.RequestCreateCharacter.Builder builder = Protocol.RequestCreateCharacter.newBuilder();
		builder.setUserID(userID);
		builder.setName(userName);
		builder.setGender(0);

		sendMessage(Protocol.MessageType.REQUEST_CREATE_CHARACTER_VALUE, builder.build().toByteArray());
	}

	public void sendRequestStartGame() {
		System.out.println("Client.sendRequestStartGame()");
		this.state = State.WAIT;
		Protocol.RequestStartGame.Builder builder = Protocol.RequestStartGame.newBuilder();
		builder.setUserID(userID);

		sendMessage(Protocol.MessageType.REQUEST_START_GAME_VALUE, builder.build().toByteArray());
	}

	public void sendRequestUpdatePosition() {
		System.out.println("GameClient.sendRequestUpdatePosition()");
		
		Protocol.RequestUpdatePosition request = Protocol.RequestUpdatePosition.newBuilder().setUserID(userID)
				.setMapID(mapID).setX(positionX).setY(positionY).setState(1).build();
		
		sendMessage(Protocol.MessageType.REQUEST_UPDATE_POSITION_VALUE, request.toByteArray());
	}

	public void sendRequestGetItems() {
		System.out.println("Client.sendRequestGetItems()");
		Protocol.RequestGetItems request = Protocol.RequestGetItems.newBuilder()
				.setUserID(userID).build();
		
		sendMessage(Protocol.MessageType.REQUEST_GET_ITEMS_VALUE, request.toByteArray());
	}
	
	public void sendRequestUpdateAction() {
		System.out.println("Client.sendRequestUpdateAction()");
		
		Protocol.RequestUpdateAction.Builder builder = Protocol.RequestUpdateAction.newBuilder();
		builder.setUserID(userID);
		builder.addActions(Protocol.CharacterAction.newBuilder()
				.setState(10)
				.setActionCommand(100)
				.setType(10)
				.setTimeRecord(100).build());
		builder.addActions(Protocol.CharacterAction.newBuilder()
				.setState(20)
				.setActionCommand(200)
				.setType(100)
				.setTimeRecord(200).build());
		sendMessage(Protocol.MessageType.REQUEST_UPDATE_ACTION_VALUE, builder.build().toByteArray());
	}
	
	public void sendRequestGetPrototype() {
		System.out.println("Client.sendRequestGetPrototype()");
		
		Protocol.RequestGetPrototype request = Protocol.RequestGetPrototype.newBuilder()
				.build();
		
		sendMessage(Protocol.MessageType.REQUEST_GET_PROTOTYPE_VALUE, request.toByteArray());		
	}
	
	public void responseLogin(Protocol.ResponseLogin response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {
			this.userID = response.getUserID();
			
			System.out.println("Login success " + userID);
			
			if (response.getHasCharacter()) {
				sendRequestGetCharacter();
			} else {
				sendRequestCreateCharacter();
			}
		} else {
			if (response.getMessage().contains("Invalid username.")) {
				sendRequestRegister();
			} else {
				System.out.println(response.getMessage());
				this.state = State.STOP;
			}
			
		}
	}

	public void responseRegister(Protocol.ResponseRegister response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {
			System.out.println("Register success");
			sendRequestLogin();
		} else {
			this.state = State.STOP;
		}
	}

	public void responseGetCharacter(Protocol.ResponseGetCharacter response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {			
			this.mapID 		= response.getCharacter().getMapID();
			this.positionX 	= response.getCharacter().getX();
			this.positionY 	= response.getCharacter().getY();
			
			sendRequestStartGame();
		} else {

		}
	}

	public void responseCreateCharacter(Protocol.ResponseCreateCharacter response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {
			
			sendRequestGetCharacter();
		}
	}

	public void responseStartGame(Protocol.ResponseStartGame response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {
			this.state = State.RUN;
		}
	}

	public void responseUpdatePosition(Protocol.ResponseUpdatePosition response) {
		if (response.getResult() == Protocol.ResponseCode.SUCCESS) {
//			positionX = rand.nextDouble() * 100;
//			positionY = rand.nextDouble() * 100;
			positionX += 0.0005;
		}
	}

	public void responseGetItems(Protocol.ResponseGetItems response) {
		if(response.getResult() == Protocol.ResponseCode.SUCCESS) {
			System.out.println("Client.responseGetItems()");
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
}
