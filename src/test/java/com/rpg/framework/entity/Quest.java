package com.rpg.framework.entity;

public class Quest {
	public enum QuestState {
		AVAILABLE, IN_PROCESS, COMPLETED
	}

	private int ID;
	private int step;
	private double percent;
	private int state;

	public Quest() {
		ID = -1;
		step = -1;
		percent = 0.0;
		state = 0;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}
