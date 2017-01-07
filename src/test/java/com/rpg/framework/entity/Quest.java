package com.rpg.framework.entity;

import java.util.ArrayList;
import java.util.List;

public class Quest {
	public enum QuestState {
		AVAILABLE, IN_PROCESS, COMPLETED
	}

	private int ID;
	private int step;
	private double percent;
	private int state;
	private List<Integer> Progress;

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

	public List<Integer> getProgress() {
		return Progress;
	}

	public void setProgress(List<Integer> progress) {
		Progress = progress;
	}

	public void setProgressList(List<Object> list) {
		Progress = new ArrayList();
		for (Object object : list) {
			Integer value = (Integer) object;
			Progress.add(value.intValue());
		}
		
	}
}
