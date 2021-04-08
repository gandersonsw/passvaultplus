/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 Parse and foramt dates. There are 3 modes: localized-date, localized-datetime, serlized-dateTime
*/
public class DateUtil {

	// Format like: "2001-07-04T12:08:56-07:00" 
	private static final DateFormat dfForSerialization = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	
	// Format like: "May 2, 2010 4:41 PM"
	private static final DateFormat dfForLocalizedDisplay = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US); // TODO - actually do localization
	
	private static final DateFormat dfNoTime = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
	private static final long TEN_DAYS = 10L * 24L * 60L * 60L * 1000L;
	
	public static String formatDateTime(final Date d, boolean localized) {
		if (d == null) {
			return "";
		}
		return (localized ? dfForLocalizedDisplay : dfForSerialization).format(d);
	}
	
	public static String formatDateTimeForSerialization(final Date d) {
		if (d == null) {
			return "";
		}
		return dfForSerialization.format(d);
	}

	public static Date parseDateTimeForSerialization(final String d) throws ParseException {
		if (d == null || d.length() == 0) {
			return null;
		}

		// TODO delete this 
		try {
			return dfForLocalizedDisplay.parse(d);
		} catch (Exception e) {
		}
		// end TODO 
		
		try {
			return dfForSerialization.parse(d);
		} catch (Exception e) {
			return dfNoTime.parse(d);
		}
	}
	
	public static String formatDateTimeLocalized(final Date d) {
		if (d == null) {
			return "";
		}
		
		return dfForLocalizedDisplay.format(d);
	}
	
	public static Date parseDateLocalized(final String d) throws ParseException {
		if (d == null || d.length() == 0) {
			return null;
		}

		try {
			return dfForLocalizedDisplay.parse(d);
		} catch (Exception e) {
			//return dfNoTime.parse(d);
		}
		
		try {
			return dfForSerialization.parse(d);
		} catch (Exception e) {
			return dfNoTime.parse(d);
		}
	}
	
	/**
	 * Parse a date String. Then set the year so that it is the next one in the future
	 * starting from (today-10 days). Return null if it wasn't parseable.
	 */
	public static Date parseUpcomingDate(final String s) {
		Date d = null;
		try {
			d = DateUtil.parseDateLocalized(s);
			Calendar cal2 = Calendar.getInstance();
			int curYear = cal2.get(Calendar.YEAR);
			cal2.setTime(d);
			cal2.set(Calendar.YEAR, curYear);
			if (!cal2.getTime().after(new Date(System.currentTimeMillis() - TEN_DAYS))) {
				cal2.add(Calendar.YEAR, 1);
			}
			return cal2.getTime();
		} catch (Exception e) {
			return tryParseMonthDay(s);
		}
	}
	
	public static Date tryParseMonthDay(final String s) {
		String[] parts = s.split("\\s");
		if (parts.length > 1) {
			int calMonth = getCalMonth(parts[0]);
			if (calMonth == -99) {
				return null;
			}
			
			int day = 0;
			try {
				day = Integer.parseInt(getIntegerOnly(parts[1]));
			} catch (Exception e) {
				return null;
			}
			
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MONTH, calMonth);
			cal.set(Calendar.DAY_OF_MONTH, day);
			
			if (cal.getTime().after(new Date(System.currentTimeMillis() - TEN_DAYS))) {
				return cal.getTime();
			} else {
				cal.add(Calendar.YEAR, 1);
				return cal.getTime();
			}
		}
	
		return null;
	}
	
	private static String getIntegerOnly(final String s) {
		int i = 0;
		while (i < s.length() && Character.isDigit(s.charAt(i))) {
			i++;
		}
		if (i == s.length()) {
			return s;
		} else {
			return s.substring(0, i);
		}
	}
	
	private static int getCalMonth(final String monthRaw) {
		
		if (monthRaw == null || monthRaw.length() < 3) {
			return -99;
		}
		
		String month = monthRaw.substring(0, 3).toLowerCase();
		
		int calMonth = -1;
		if (month.equals("jan")) {
			calMonth = Calendar.JANUARY;
		} else if (month.equals("feb")) {
			calMonth = Calendar.FEBRUARY;
		} else if (month.equals("mar")) {
			calMonth = Calendar.MARCH;
		} else if (month.equals("apr")) {
			calMonth = Calendar.APRIL;
		} else if (month.equals("may")) {
			calMonth = Calendar.MAY;
		} else if (month.equals("jun")) {
			calMonth = Calendar.JUNE;
		} else if (month.equals("jul")) {
			calMonth = Calendar.JULY;
		} else if (month.equals("aug")) {
			calMonth = Calendar.AUGUST;
		} else if (month.equals("sep")) {
			calMonth = Calendar.SEPTEMBER;
		} else if (month.equals("oct")) {
			calMonth = Calendar.OCTOBER;
		} else if (month.equals("nov")) {
			calMonth = Calendar.NOVEMBER;
		} else if (month.equals("dec")) {
			calMonth = Calendar.DECEMBER;
		} else {
			calMonth = -99;
		}
		return calMonth;
	}

	/**
	 * A format like 23:59:59.999
	 * With HH:HH:SS.mmm
	 * with current time
	 */
	public static String getMillisecondTimeStamp() {
			return getMillisecondTimeStamp(null);
	}

	/**
	 * A format like 23:59:59.999
	 * With HH:HH:SS.mmm
	 */
	public static String getMillisecondTimeStamp(Date d) {
			Calendar c = Calendar.getInstance();
			if (d != null) {
					c.setTime(d);
			}
			return String.format("%02d:%02d:%02d.%03d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND), c.get(Calendar.MILLISECOND));
	}

  /**
	 * A format like -2016-8-28-15
	 * With -year-month-day-hour
	 */
	public static String getHourlyTimeStamp() {
		return getHourlyTimeStamp(null);
	}

	public static String getHourlyTimeStamp(Date d) {
		Calendar c = Calendar.getInstance();
		if (d != null) {
			c.setTime(d);
		}
		StringBuffer ret = new StringBuffer();
		ret.append("-");
		ret.append(c.get(Calendar.YEAR));
		ret.append("-");
		ret.append(1 + c.get(Calendar.MONTH));
		ret.append("-");
		ret.append(c.get(Calendar.DAY_OF_MONTH));
		ret.append("-");
		ret.append(c.get(Calendar.HOUR_OF_DAY));
		return ret.toString();
	}

	public static Date parseHourlyTimeStamp(String ts) {
		String[] parts = ts.split("-");
		for (int ioffs = 0; ioffs < parts.length - 3; ioffs++) {
			try {
				int year = Integer.parseInt(parts[ioffs]);
				int month = Integer.parseInt(parts[ioffs + 1]);
				int day = Integer.parseInt(parts[ioffs + 2]);
				int hour;
				if (parts[ioffs + 3].length() > 1 && Character.isDigit(parts[ioffs + 3].charAt(1))) {
					hour = Integer.parseInt(parts[ioffs + 3].substring(0,2));
				} else {
					hour = Integer.parseInt(parts[ioffs + 3].substring(0,1));
				}

				Calendar c = Calendar.getInstance();
				c.set(year, month - 1, day, hour, 0, 0);
				return c.getTime();
			} catch (Exception e) {
				// ignore this
			}
		}

		return new Date(); // return now as a default
	}

}
