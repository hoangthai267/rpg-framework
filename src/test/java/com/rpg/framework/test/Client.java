package com.rpg.framework.test;

import com.google.protobuf.InvalidProtocolBufferException;
import com.rpg.framework.client.SocketClient;
import com.rpg.framework.database.Protocol;

public class Client extends SocketClient {
	private String host;
	private int port;

	public Client(String host, int port) {
		super(host, port);
		this.host = host;
		this.port = port;
	}

	public void start() {
		super.start();
		sendRegisterRequest();
	}

	public void stop() {
		super.stop();
	}

	public void update() {

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

	public void send(int commandID, byte[] data) {
		super.send(commandID, data);
	}

	public void receive(int commandID, byte[] data) {
		super.receive(commandID, data);
		try {
			switch (commandID) {
			case Protocol.MessageType.RESPONE_LOGIN_VALUE: {
				System.out.println(Protocol.ResponseLogin.parseFrom(data));
				break;
			}
			case Protocol.MessageType.RESPONE_REGISTER_VALUE: {
				System.out.println(Protocol.ResponseRegister.parseFrom(data));
				break;
			}
			case Protocol.MessageType.RESPONE_LIST_OF_CHARACTER_VALUE: {
				System.out.println(Protocol.ResponseListOfCharacter.parseFrom(data));
				break;
			}
			case Protocol.MessageType.RESPONE_CREATE_CHARACTER_VALUE: {
				System.out.println(Protocol.ResponseCreateCharacter.parseFrom(data));
				break;
			}
			case Protocol.MessageType.RESPONE_START_GAME_VALUE: {
				System.out.println(Protocol.ResponseStartGame.parseFrom(data));
				break;
			}
			case Protocol.MessageType.RESPONE_UPDATE_POSITION_VALUE: {
				System.out.println(Protocol.ResponseUpdatePosition.parseFrom(data));
			}
			default:
				break;
			}

		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		stop();
	}

	public void sendLoginRequest() {
		Protocol.RequestLogin.Builder builder = Protocol.RequestLogin.newBuilder();
		builder.setUsername("admin1");
		builder.setPassword("admin");

		send(Protocol.MessageType.REQUEST_LOGIN_VALUE, builder.build().toByteArray());
	}

	public void sendRegisterRequest() {
		Protocol.RequestRegister.Builder builder = Protocol.RequestRegister.newBuilder();
		builder.setUsername("admin");
		builder.setPassword("admin");

		send(Protocol.MessageType.REQUEST_REGISTER_VALUE, builder.build().toByteArray());
	}

	public void requestListOfCharacter() {
		Protocol.RequestListOfCharacter.Builder builder = Protocol.RequestListOfCharacter.newBuilder();
		builder.setUserID("User_1");

		send(Protocol.MessageType.REQUEST_LIST_OF_CHARACTER_VALUE, builder.build().toByteArray());
	}

	public void requestCreateCharacter() {
		Protocol.RequestCreateCharacter.Builder builder = Protocol.RequestCreateCharacter.newBuilder();
		builder.setUserID("User_1");
		builder.setName("WA");
		builder.setOccupation("WA");

		send(Protocol.MessageType.REQUEST_CREATE_CHARACTER_VALUE, builder.build().toByteArray());
	}

	public void requestStartGame() {
		Protocol.RequestStartGame.Builder builder = Protocol.RequestStartGame.newBuilder();
		builder.setUserID("User_1");
		builder.setCharID("Character_1");

		send(Protocol.MessageType.REQUEST_START_GAME_VALUE, builder.build().toByteArray());
	}

	public void requestUpdatePosition() {
		Protocol.RequestUpdatePosition request = Protocol.RequestUpdatePosition.newBuilder()
				.setUserID("User_1")
				.setCharID("Character_1")
				.setNewPosition(Protocol.CharacterPosition.newBuilder().setMapID("Map_1").setX(0.0).setY(0.0).build())
				.build();
		
		send(Protocol.MessageType.REQUEST_UPDATE_POSITION_VALUE, request.toByteArray());
	}

	public static void main(String args[]) {
		new Client("localhost", 8463).start();
	}
}
