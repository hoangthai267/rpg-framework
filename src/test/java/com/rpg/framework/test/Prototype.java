package com.rpg.framework.test;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

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
		couchbase 		= new CouchBase("Static");
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
		for(int i = 0; i < 3; i++) {
			mapArray.add(JsonObject.create()
					.put("id", i)
					.put("width", 200 * i)
					.put("height", 300 *i)
					.put("monsters", JsonArray.create()
							.add(JsonObject.create()
									.put("id", 1)
									.put("index", 1 + 3 * i)
									.put("x", 0.0)
									.put("y", 0.0))
							.add(JsonObject.create()
									.put("id", 2)
									.put("index", 2 + 3 * i)
									.put("x", 0.0)
									.put("y", 0.0))
							.add(JsonObject.create()
									.put("id", 3)
									.put("index", 3 + 3 * i)
									.put("x", 0.0)
									.put("y", 0.0)))
					.put("items", JsonArray.create()
							.add(JsonObject.create()
									.put("id", 1)
									.put("x", 0.0)
									.put("y", 0.0))
							.add(JsonObject.create()
									.put("id", 2)
									.put("x", 0.0)
									.put("y", 0.0))
							.add(JsonObject.create()
									.put("id", 3)
									.put("x", 0.0)
									.put("y", 0.0)))
					.put("portals", JsonArray.create()
							.add(JsonObject.create()
									.put("id", 1 + i)
									.put("x", 0.0)
									.put("y", 0.0)
									.put("destination", 1))
							.add(JsonObject.create()
									.put("id", 2 + i)
									.put("x", 0.0)
									.put("y", 0.0)
									.put("destination", 2))
							.add(JsonObject.create()
									.put("id", 3 + i)
									.put("x", 0.0)
									.put("y", 0.0)
									.put("destination", 3)))
					);
		}
				
		
		JsonObject maps = JsonObject.create()
				.put("normalMaps", mapArray)
				.put("hiddenMaps", mapArray);
		
		couchbase.set("Prototype_Maps", maps);				
	}
	
	public void addPrototypeMonsters() {
		JsonArray data = JsonArray.create()
				.add(JsonObject.create()
						.put("id", 1)
						.put("maxHP", 100)
						.put("maxMP", 100)
						.put("damage", 10)
						.put("defense", 10)
						.put("speed", 10))
				.add(JsonObject.create()
						.put("id", 2)
						.put("maxHP", 200)
						.put("maxMP", 200)
						.put("damage", 20)
						.put("defense", 20)
						.put("speed", 20))
				.add(JsonObject.create()
						.put("id", 3)
						.put("maxHP", 300)
						.put("maxMP", 300)
						.put("damage", 30)
						.put("defense", 30)
						.put("speed", 30));
		JsonObject document = JsonObject.create()
				.put("data", data)
				.put("total", 3);
		
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
	
	public void initialize() {
		addPrototypeItems();
		addPrototypeMap();
		addPrototypeMonsters();
	}

	
	public static void main(String args[]) {
		Prototype prototype = new Prototype();
		prototype.initialize();
	}
}

