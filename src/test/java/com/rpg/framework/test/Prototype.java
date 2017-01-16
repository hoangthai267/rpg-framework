package com.rpg.framework.test;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.rpg.framework.data.CouchBase;
import com.rpg.framework.database.Protocol;

import com.google.protobuf.InvalidProtocolBufferException;

import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Parser;
import com.google.protobuf.util.JsonFormat.Printer;

public class Prototype {
	private CouchBase couchbase;
	private JsonObject items;
	private JsonArray useItems;
	private JsonArray collectItems;
	private JsonArray equipItems;
	private int	total;
	private Parser parser;
	private Printer printer;
	
	public Prototype() {
//		couchbase 		= new CouchBase("128.199.255.44","Static");
		couchbase 		= new CouchBase("127.0.0.1","Static");
		try {
			items = couchbase.get("Prototype_Items");
			useItems = items.getArray("use");
			collectItems = items.getArray("collect");
			equipItems = items.getArray("equip");
			total = items.getInt("total");
		} catch (Exception ex) {
			total = 0;
			useItems = JsonArray.create();
			collectItems = JsonArray.create();
			equipItems = JsonArray.create();
			items = JsonObject.create()
					.put("use", useItems)
					.put("collect", collectItems)
					.put("equip", equipItems)
					.put("total", total);
			couchbase.set("Prototype_Items", items);
		}
		
		parser = JsonFormat.parser();
		printer = JsonFormat.printer();
	}
	
	public void addUseItem(int HPValue, int MPValue, int duration, int hpPerSecond, int mpPerSecond, float bonusExp, float bonusItemDrop) {
		total++;
		Protocol.Use use = Protocol.Use.newBuilder()
				.setID(total)
				.setHPValue(HPValue)
				.setMPValue(MPValue)
				.setDuration(duration)
				.setHPPerSecond(hpPerSecond)
				.setMPPerSecond(mpPerSecond)
				.setBonusExp(bonusExp)
				.setBonusItemDrop(bonusItemDrop)
				.build();
		
		try {
			useItems.add(JsonObject.fromJson(printer.print(use)));
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		items.put("use", useItems);
		items.put("total", total);
		
		couchbase.set("Prototype_Items", items);
	}
	
	public void addCollectItems(int type) {
		total++;
		Protocol.Collect collect = Protocol.Collect.newBuilder()
				.setID(total)
				.setType(type)
				.build();
		
		try {
			collectItems.add(JsonObject.fromJson(printer.print(collect)));
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		items.put("use", collectItems);
		items.put("total", total);
		
		couchbase.set("Prototype_Items", items);
	}
	
	public void addEquipItem(int type, int hpBonus, float hpPercentBonus) {
		total++;
		
		Protocol.Equip equip = Protocol.Equip.newBuilder()
				.setID(total)
				.setType(type)
				.setBonusStats(Protocol.BonusStats.newBuilder()
						.setBonusHP(hpBonus)
						.setBonusPercentHP(hpPercentBonus)
						.build())
				.build();
		
		try {
			equipItems.add(JsonObject.fromJson(printer.print(equip)));
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		items.put("equip", equipItems);
		items.put("total", total);
		
		couchbase.set("Prototype_Items", items);
	}
	
	public void function() {
		Protocol.Equip.Builder builder = Protocol.Equip.newBuilder();
		try {
			parser.merge(equipItems.get(0).toString(), builder);
			System.out.println(builder.build());
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addPrototypeMap() {
		JsonArray mapArray = JsonArray.create();
		int index = 1;
//		for(int i = 0; i < 3; i++) {
//			JsonObject map = JsonObject.create();
//			mapArray.add(map
//					.put("id", i)
//					.put("width", 200 * i)
//					.put("height", 300 *i));
//			JsonArray monsters = JsonArray.create();
//			for (int j = 0; j < Config.MAP1_SNAIL.length; j += 2) {
//				monsters.add(JsonObject.create()
//									.put("id", 1)
//									.put("index", index++)
//									.put("x", Config.MAP1_SNAIL[j])
//									.put("y", Config.MAP1_SNAIL[j + 1]));
//			}	
//			map.put("monsters", monsters);
//		}
		
		JsonObject map1 = JsonObject.create()
				.put("id", 1)
				.put("width", 200)
				.put("height", 300);				
		JsonArray monsters = JsonArray.create();
		for (int j = 0; j < Config.MAP1_SNAIL.length; j += 2) {
			monsters.add(JsonObject.create()
								.put("id", 1)
								.put("index", index++)
								.put("x", Config.MAP1_SNAIL[j])
								.put("y", Config.MAP1_SNAIL[j + 1]));
		}	
		map1.put("monsters", monsters);				

		JsonObject map2 = JsonObject.create()
				.put("id", 1)
				.put("width", 200)
				.put("height", 300);
		
		monsters = JsonArray.create();
		for (int j = 0; j < Config.MAP2_MONKEY.length; j += 2) {
			monsters.add(JsonObject.create()
								.put("id", 2)
								.put("index", index++)
								.put("x", Config.MAP2_MONKEY[j])
								.put("y", Config.MAP2_MONKEY[j + 1]));
		}	
		map2.put("monsters", monsters);	
		
		
		mapArray.add(map1).add(map2);
		
		JsonObject maps = JsonObject.create()
				.put("normalMaps", mapArray);
		
		couchbase.set("Prototype_Maps", maps);				
	}
	
	public void addPrototypeMonsters() {
		JsonArray data = JsonArray.create();
		
		for(int i = 0; i < Config.NUMBER_OF_MONSTER_TYPE; i++) {
			data.add(JsonObject.create()
					.put("id", i + 1)
					.put("maxHP", 	Config.MONSTER_HP[i])
					.put("maxMP", 	Config.MONSTER_MP[i])
					.put("damage", 	Config.MONSTER_DAMAMGE[i])
					.put("defense", Config.MONSTER_DEFENSE[i])
					.put("speed", 	Config.MONSTER_SPEED[i]));
		}
		
		JsonObject document = JsonObject.create()
				.put("data", data)
				.put("total", Config.NUMBER_OF_MONSTER_TYPE);
		
		couchbase.set("Prototype_Monsters", document);
		
	}
	
	public void addPrototypeItems() {
		addCollectItems(1);
		addCollectItems(2);
		addCollectItems(3);
		
		addEquipItem(1, 10, 1.0f);
		addEquipItem(3, 20, 2.0f);
		addEquipItem(2, 30, 3.0f);
		
		addUseItem(100, 0, 10, 10, 0, 0, 0);
		addUseItem(0, 100, 10, 0, 10, 0, 0);
		addUseItem(50, 50, 10, 5, 5, 0, 0);
	}
	
	public void addAccount() {
		JsonArray data = JsonArray.create()
				.add(JsonObject.create()
						.put("username", "admin")
						.put("password", "admin")
						.put("id", 0))
				.add(JsonObject.create()
						.put("username", "admin1")
						.put("password", "admin")
						.put("id", 1));
		
		JsonObject document = JsonObject.create()
				.put("total", 0);
		
		couchbase.set("Accounts", document);
	}
	
	public void addPrototypeCharacter() {
		JsonObject data = JsonObject.create()
				.put("damage", 			Config.CHARACTER_DAMAGE)
				.put("defense", 		Config.CHARACTER_DEFENSE)
				.put("speed", 			Config.CHARACTER_SPEED)
				.put("numberOfAttack", 	Config.CHARACTER_NUMBER_OF_ATTACK)
				.put("maxHP",	 		Config.CHARACTER_HP)
				.put("maxMP", 			Config.CHARACTER_MP);
		
		couchbase.set("Prototype_Character", data);
	}
	
	public void initialize() {
		addAccount();
		addPrototypeItems();
		addPrototypeMap();
		addPrototypeMonsters();
//		addPrototypeCharacter();
	}
	
	public void query() {
		String statement = "SELECT admin1.* FROM Static USE KEYS \"Accounts\" WHERE admin1.`password` = \"admin\";";
		N1qlQueryResult result = couchbase.query(statement);
		System.out.println(result);
		System.out.println(result.rows().next().value().getInt("userID"));
		for (N1qlQueryRow row : result) {
		}		
	}

	
	public static void main(String args[]) {
//		System.out.println(String.format("SELECT %s %.2f", "abc", 1.0f));
		Prototype prototype = new Prototype();
		prototype.initialize();
	}
}

