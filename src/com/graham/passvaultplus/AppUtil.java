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
		return df.parse(d);
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
