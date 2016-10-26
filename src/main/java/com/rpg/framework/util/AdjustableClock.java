package com.rpg.framework.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class AdjustableClock extends Clock {
	long m_freezeTime = 0;
	boolean m_isFreeze = false;
	long m_milliSecAdd;

	public AdjustableClock() {
		m_milliSecAdd = 0;
	}

	/**
	 * @param MillisecondAdd
	 *            Milliseconds to be added to the result of currentTimeMillis A
	 *            positive number means it will run faster, negative number
	 *            means it will slower.
	 */
	public AdjustableClock(long MillisecondAdd) {
		m_milliSecAdd = MillisecondAdd;
	}

	public AdjustableClock(String TimeStart) throws ParseException {
		long secStart = Time.GetSecOfDatetime(TimeStart);
		m_milliSecAdd = secStart * 1000 - System.currentTimeMillis();
	}

	public long currentTimeMillis() {
		if (m_isFreeze) {
			return m_freezeTime;
		}
		return m_milliSecAdd + System.currentTimeMillis();
	}

	public Calendar GetCalendarInstance() {
		Calendar res = GregorianCalendar.getInstance();
		res.setTimeInMillis(this.currentTimeMillis());
		return res;
	}

	public static AdjustableClock GetClockNextDay() {
		return new AdjustableClock(24 * 3600000 + 1);
	}

	/**
	 * */
	public static AdjustableClock GetClockAtDatetime(String DateTimeStart) throws ParseException {
		long sec = Time.GetMillisecOfDate(DateTimeStart);
		long delta = sec - System.currentTimeMillis();
		return new AdjustableClock(delta);
	}

	/**
	 * */
	public static AdjustableClock GetClockAtSecond(long Second) {
		long delta = Second * 1000 - System.currentTimeMillis();
		return new AdjustableClock(delta);
	}

	public long Freeze() {
		m_freezeTime = Time.currentTimeMillis();
		m_isFreeze = true;
		return m_freezeTime;
	}

	public long UnFreeze() {
		m_freezeTime = Time.currentTimeMillis();
		m_isFreeze = false;
		return m_freezeTime;
	}

	@Override
	public long MoveForward(long AddMilliSec) {
		m_milliSecAdd += AddMilliSec;
		return currentTimeMillis();
	}
}
