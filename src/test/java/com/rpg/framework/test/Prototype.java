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

	
	public static void main(String args[]) {
		Prototype prototype = new Prototype();
//		prototype.function();
		prototype.addCollectItems(1);
//		prototype.addCollectItems(2);
//		prototype.addCollectItems(3);
//		prototype.addUseItem(100, 0, 10, 10, 0, 0, 0);
//		prototype.addUseItem(0, 100, 10, 0, 10, 0, 0);
		prototype.addUseItem(50, 50, 10, 10, 10, 0, 0);
		prototype.addEquipItem(1, 50, 5.0f);
//		prototype.addEquipItem(2, 100, 5.0f);
	}
}
