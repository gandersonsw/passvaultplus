/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.util;

public class StringUtil {

	public static String limitStrLen(final String s, final int maxLen) {
		if (s.length() <= maxLen) {
			return s;
		}

		return s.substring(0, maxLen - 3) + "...";
	}

	public static boolean equalsWithEmpty(final String s1, final String s2) {
		if (stringEmpty(s1)) {
			return stringEmpty(s2);
		}
		return s1.equals(s2);
	}

	public static boolean stringEmpty(String s) {
		return s == null || s.length() == 0;
	}

	public static int stringLength(String s) {
		if (s == null) {
			return 0;
		}
		return s.length();
	}

	public static boolean stringNotEmpty(String s) {
		return s != null && s.length() > 0;
	}

	public static boolean stringNotEmptyTrim(String s) {
		return s != null && s.trim().length() > 0;
	}

	public static Integer tryParseInt(String s) {
		if (stringEmpty(s)) {
			return null;
		}
		try {
			int i = Integer.parseInt(s);
			return new Integer(i);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static String repeatString(String s, Integer rc) {
		if (rc == null) {
			return s;
		}
		int irc = rc.intValue();
		if (irc == 0) {
			return "";
		}
		if (irc == 1) {
			return s;
		}
		StringBuffer sb = new StringBuffer(s.length() * irc);
		for (int i = 0; i < irc; i++) {
			sb.append(s);
		}
		return sb.toString();
	}

	public static String replaceAll(String s, String searchString, String replaceWithString) {

		if (s.indexOf(searchString) == -1)
			return s;

		StringBuffer ret = new StringBuffer();

		int i = 0;
		int previ = 0;
		while ((i = s.indexOf(searchString, i)) != -1) {
			ret.append(s.substring(previ,i));
			ret.append(replaceWithString);
			i += searchString.length();
			previ = i;
		}

		ret.append(s.substring(previ));
		return ret.toString();
	}

	public static String getPluralAppendix(final int count) {
		if (count == 1) {
			return "";
		}
		return "s";
	}

	public static int dataInString(String s) {
		byte[] sbytes = s.getBytes();

		int[] counts = new int[256];
		for (byte b : sbytes) {
			int bi = (int)b + 128;
			counts[bi]++;
		}
		int numberOfValues = 0;
		for (int count : counts) {
			if (count > 0) {
				numberOfValues++;
			}
		}
		return (int)((Math.log(numberOfValues + 1) / Math.log(2)) * s.length());
	}

}
