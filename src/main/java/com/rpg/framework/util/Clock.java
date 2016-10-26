package com.rpg.framework.util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author hungpt2
 */
public class Clock {

	public Clock() {

	}

	public long currentTimeNano() {
		return System.nanoTime();
	}

	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	public Calendar GetCalendarInstance() {
		Calendar res = GregorianCalendar.getInstance();
		return res;
	}

	public long MoveForward(long AddMilliSec) {
		throw new UnsupportedOperationException("Not support move clock forward.");
	}

}
