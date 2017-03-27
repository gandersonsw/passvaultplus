/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AppUtil {

	// Format like: "May 2, 2010 4:41 PM"
	private static DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
	private static DateFormat dfNoTime = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
	
	public static String formatDate1(final Date d) {
		if (d == null) {
			return "";
		}
		
		return df.format(d);
	}

	public static Date parseDate1(final String d) throws ParseException {
		if (d == null || d.length() == 0) {
			return null;
		}

		try {
			return df.parse(d);
		} catch (Exception e) {
			return dfNoTime.parse(d);
		}
	}
	
	/**
	 * Parse a date String. Then set the year so that it is the next one in the future starting from today. Return null if it wasn't parseable
	 */
	public static Date parseUpcomingDate(final String s) {
		Date d = null;
		try {
			d = AppUtil.parseDate1(s);
			Calendar cal2 = Calendar.getInstance();
			int curYear = cal2.get(Calendar.YEAR);
			cal2.setTime(d);
			cal2.set(Calendar.YEAR, curYear);
			if (!cal2.getTime().after(new Date())) {
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
			
			if (cal.getTime().after(new Date())) {
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
		
		if (monthRaw == null || monthRaw.length() < 2) {
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

	public static String limitStrLen(final String s, final int maxLen) {
		if (s.length() <= maxLen) {
			return s;
		}

		return s.substring(0, maxLen - 3) + "...";
	}

	/**
	 * Check to see if a backup file has been created in the last hour. If it has not, rename the given file to the backup file name.
	 */
	public static void checkBackupFileHourly(final File f) {
		String filenameParts[] = getFileNameParts(f.getName());
		String timeStamp = getHourlyTimeStamp();
		File backupFile = new File(f.getParentFile(), filenameParts[0] + "-" + timeStamp + "." + filenameParts[1]);
		// don't backup if a backup has been done within the last hour
		if (! backupFile.exists()) {
			f.renameTo(backupFile);
		}
	}

	/**
	 * Get the file name without a suffix, and the file suffix.
	 * Will always return an array length 2. 1st item is name, 2nd item is file-suffix
	 */
	public static String[] getFileNameParts(final String fileName) {
		int dotLoc = fileName.indexOf(".");
		if (dotLoc == -1) {
			String[] ret = { fileName, ""};
			return ret;
		}

		String fileNameWithNoSuffix = fileName.substring(0, dotLoc);
		String fileSuffix = fileName.substring(dotLoc + 1);
		String[] ret = { fileNameWithNoSuffix, fileSuffix};
		return ret;
	}

  /**
	 * A format like 2016-8-28-15
	 * With year-month-day-hour
	 */
	public static String getHourlyTimeStamp() {
		Calendar c = Calendar.getInstance();
		StringBuffer ret = new StringBuffer();
		ret.append(c.get(Calendar.YEAR));
		ret.append("-");
		ret.append(1 + c.get(Calendar.MONTH));
		ret.append("-");
		ret.append(c.get(Calendar.DAY_OF_MONTH));
		ret.append("-");
		ret.append(c.get(Calendar.HOUR_OF_DAY));
		return ret.toString();
	}

	public static String getExceptionStackTrace(final Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	public static boolean equalsWithNull(final Object obj1, final Object obj2) {
		if (obj1 == null && obj2 == null) {
			return true;
		}
		if (obj1 == null || obj2 == null) {
			return false;
		}
		return obj1.equals(obj2);
	}

}
