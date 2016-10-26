package com.rpg.framework.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Time {
	public final static int SECOND_IN_MINUTE = 60;
	public final static int SECOND_IN_HOUR = 60 * SECOND_IN_MINUTE;
	public final static int SECOND_IN_DAY = 24 * SECOND_IN_HOUR;
	public final static int SECOND_IN_3_DAY = 3 * SECOND_IN_DAY;
	public final static int SECOND_IN_7_DAY = 7 * SECOND_IN_DAY;
	public final static int SECOND_IN_10_DAY = 10 * SECOND_IN_DAY;
	public final static int SECOND_IN_14_DAY = 14 * SECOND_IN_DAY;
	public final static int SECOND_IN_30_DAY = 30 * SECOND_IN_DAY;
	public final static int SECOND_IN_31_DAY = 31 * SECOND_IN_DAY;
	public final static int SECOND_IN_YEAR = 365 * SECOND_IN_DAY;
	public final static int SECOND_IN_90_DAY = 90 * SECOND_IN_DAY;

	public final static long MILLISECOND_IN_MINUTE = 1000 * SECOND_IN_MINUTE;
	public final static long MILLISECOND_IN_HOUR = 1000 * SECOND_IN_HOUR;
	public final static long MILLISECOND_IN_DAY = 1000 * SECOND_IN_DAY;
	public final static long MILLISECOND_IN_7_DAY = 1000 * SECOND_IN_7_DAY;
	public final static long MILLISECOND_IN_30_DAY = 1000 * SECOND_IN_30_DAY;
	public final static long MILLISECOND_IN_31_DAY = 1000 * SECOND_IN_31_DAY;

	public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static final String DATE_TIME_FORMAT_SHORT = "yyyyMMdd HHmmss";
	public static final String DATE_TIME_FORMAT_TRANSACTION = "yyMMddHHmmss";

	public static final String DATE_TIME_FORMAT_DATE_ONLY = "yyyyMMdd";

	public static final String DATE_TIME_FORMAT_FULL = "yyyyMMdd HHmmss SSS";
	public static final String DATE_TIME_FORMAT_FILE = "yyyyMMdd_HHmmss";

	private static Clock s_clock;
	static {
		s_clock = new Clock();
	}

	public static void SetClock(Clock NewClock) {
		s_clock = NewClock;
	}

	public static void SetClockForward(long MoveSeconds) {
		long curSec = currentTimeSecond();

		s_clock = new AdjustableClock((curSec + MoveSeconds) * 1000 - System.currentTimeMillis());
	}

	public static long currentTimeMillis() {

		return s_clock.currentTimeMillis();
	}

	public static long GetCurrentNano() {
		return s_clock.currentTimeNano();
	}

	public static long currentTimeSecond() {
		return s_clock.currentTimeMillis() / 1000;
	}

	public static String currentDateString(String pattern) {
		return new SimpleDateFormat(pattern, Locale.getDefault()).format(new Date());
	}

	public static String getDateString(String pattern, long milliseconds) {
		return new SimpleDateFormat(pattern, Locale.getDefault()).format(new Date(milliseconds));
	}

	public static long GetSecOfTimeOfDay(int hour, int minutes) {
		Calendar calendar = GregorianCalendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		calendar.set(year, month, day, hour, minutes, 0);

		return calendar.getTimeInMillis() / 1000;
	}

	public static int GetSecStartOfDay(long miliSecond) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(miliSecond);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return (int) (calendar.getTimeInMillis() / 1000);
	}

	public static long GetSecStartOfToday() {
		// Calendar calendar = GregorianCalendar.getInstance();
		Calendar calendar = s_clock.GetCalendarInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		calendar.set(year, month, day, 0, 0, 0);

		return calendar.getTimeInMillis() / 1000;
	}

	public static long GetSecSinceStartOfDay() {
		return Time.currentTimeSecond() - GetSecStartOfToday();
	}

	public static long GetSecStartOfMonth() {
		// Calendar calendar = GregorianCalendar.getInstance();
		Calendar calendar = s_clock.GetCalendarInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		calendar.set(year, month, 1, 0, 0, 0);

		return calendar.getTimeInMillis() / 1000;
	}

	public static long GetSecEndOfDay() {
		// Calendar calendar = GregorianCalendar.getInstance();
		Calendar calendar = s_clock.GetCalendarInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		calendar.set(year, month, day, 23, 59, 59);

		return calendar.getTimeInMillis() / 1000;
	}

	public static long GetSecEndOfDay(long Second) {
		Date thatDay = new Date(Second * 1000);
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(thatDay);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		calendar.set(year, month, day, 23, 59, 59);

		return calendar.getTimeInMillis() / 1000;
	}

	/*
	 * @return Return how many days different between 2 time value (in seconds)
	 */
	public static int DayDiff(long Sec1, long Sec2) {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(Sec1 * 1000);
		int d1 = cal.get(Calendar.DAY_OF_YEAR);
		cal.setTimeInMillis(Sec2 * 1000);
		int d2 = cal.get(Calendar.DAY_OF_YEAR);
		return Math.abs(d2 - d1);
	}

	public static int MonthDiff(long StartSec, long EndSec) {
		Calendar start = GregorianCalendar.getInstance();
		start.setTimeInMillis(StartSec * 1000);
		Calendar end = GregorianCalendar.getInstance();
		end.setTimeInMillis(EndSec * 1000);

		int startYear = start.get(Calendar.YEAR);
		int startMonth = start.get(Calendar.MONTH);

		int endYear = end.get(Calendar.YEAR);
		int endMonth = end.get(Calendar.MONTH);
		int monthsElapsed = (endYear - startYear) * 12 + (endMonth - startMonth);

		return Math.abs(monthsElapsed);
	}

	public static String GetDateOfSecond(long Second) {
		return GetDefaultDateFormat().format(new Date(Second * 1000));
	}

	public static String GetDateCurrent() {
		return GetDateOfSecond(Time.currentTimeSecond());
	}

	public static String GetDateCurrentShort() {
		return new SimpleDateFormat(DATE_TIME_FORMAT_SHORT, Locale.getDefault()).format(Time.currentTimeMillis());
	}

	/**
	 * Current datetime format for transaction id
	 */
	public static String GetDateCurrentTransaction() {
		return new SimpleDateFormat(DATE_TIME_FORMAT_TRANSACTION, Locale.getDefault()).format(Time.currentTimeMillis());
	}

	public static String GetDateCurrentFull() {
		return new SimpleDateFormat(DATE_TIME_FORMAT_FULL, Locale.getDefault()).format(Time.currentTimeMillis());
	}

	/***
	 * Date format for file doesn't contains spaces or colons
	 */
	public static String GetDateCurrentFile() {
		return new SimpleDateFormat(DATE_TIME_FORMAT_FILE, Locale.getDefault()).format(Time.currentTimeMillis());
	}

	public static String GetDateCurrentDateOnly() {
		return new SimpleDateFormat(DATE_TIME_FORMAT_DATE_ONLY, Locale.getDefault()).format(Time.currentTimeMillis());
	}

	public static String GetDateCurrentDateOnly(long milli_sec) {
		return new SimpleDateFormat(DATE_TIME_FORMAT_DATE_ONLY, Locale.getDefault()).format(milli_sec);
	}

	public static String GetDateCurrentDefault() {
		return new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT, Locale.getDefault()).format(Time.currentTimeMillis());
	}

	public static String GetDateCurrent(String Pattern) {
		return new SimpleDateFormat(Pattern, Locale.getDefault()).format(Time.currentTimeSecond());
	}

	public static String GetDateOfMilisecond(long Milisecond) {
		return GetDefaultDateFormat().format(new Date(Milisecond));
	}

	public static long GetMillisecOfDate(String DateString) throws ParseException {
		return GetDefaultDateFormat().parse(DateString).getTime();
	}

	public static long GetSecOfDatetime(String DateString) throws ParseException {
		return GetMillisecOfDate(DateString) / 1000;
	}

	public static SimpleDateFormat GetDefaultDateFormat() {
		return new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT, Locale.getDefault());
	}

	/**
	 * <b>For testing only</b>
	 */
	public static void SetDefaultClock() {
		Clock c = new Clock();
		s_clock = c;
	}

	/**
	 * <b>For testing only</b>
	 */
	public static void MoveClockForward(long Millisec) {
		s_clock.MoveForward(Millisec);
	}

	/**
	 * Tell the day in month at current time
	 * 
	 * @return day in month start from 1
	 */
	public static int GetDayInMonth() {
		return s_clock.GetCalendarInstance().get(Calendar.DAY_OF_MONTH);
	}

	public static Calendar GetCalendar() {
		return s_clock.GetCalendarInstance();
	}

	/**
	 * Tell the number of days in current month
	 * 
	 * @return Number of days
	 */
	public static int GetDaysInCurrentMonth() {
		return s_clock.GetCalendarInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 */
	public static int GetCurrentHour() {
		return s_clock.GetCalendarInstance().get(Calendar.HOUR_OF_DAY);
	}

	public static int GetCurrentMonth() {
		return s_clock.GetCalendarInstance().get(Calendar.MONTH);
	}

	public static int GetCurrentYear() {
		return s_clock.GetCalendarInstance().get(Calendar.YEAR);
	}

	public static String currentDateString() {
		return LocalDateTime.now().toString();
	}
}
