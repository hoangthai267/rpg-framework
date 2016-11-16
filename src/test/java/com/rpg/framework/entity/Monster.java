package com.rpg.framework.entity;

public class Monster {
	private int id;
	private int index;
	private Position position;
	private Status status;
	private Stats stats;
	private double respawnTime;
	
	public Monster() {
		this.position = new Position();
		this.stats = new Stats();
		this.status = new Status();
		this.respawnTime = 5.0f;
	}
	
	public void update(double delta) {
		if(respawnTime > 0.0) {
			respawnTime -= delta;
		} else {
			this.status.setCurHP(this.status.getCurHP() - 1);
		}		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Stats getStats() {
		return stats;
	}

	public void setStats(Stats stats) {
		this.stats = stats;
	}
	
	public boolean isDead() {
		return status.getCurHP() <= 0;
	}
	
	public Monster clone() {
		Status status = new Status();
		
		status.setMaxHP(this.status.getMaxHP());
		status.setCurHP(this.status.getCurHP());
		status.setMaxMP(this.status.getMaxMP());
		status.setCurMP(this.status.getCurMP());
		
		Stats stats = new Stats();
		stats.setDamage(this.stats.getDamage());
		stats.setDefense(this.stats.getDefense());
		stats.setSpeed(this.stats.getSpeed());
		
		Position position = new Position();
		position.setMapID(this.position.getMapID());
		position.setX(this.position.getX());
		position.setY(this.position.getY());
		
		Monster monster = new Monster();
		monster.setId(this.id);
		monster.setIndex(this.index);
		monster.setPosition(position);
		monster.setStats(stats);
		monster.setStatus(status);
		
		return monster;
	}

	public double getRespawnTime() {
		return respawnTime;
	}

	public void setRespawnTime(double respawnTime) {
		this.respawnTime = respawnTime;
	}
	
	public boolean isRespawn() {
		return respawnTime <= 0.0;
	}
}
