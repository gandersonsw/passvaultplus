/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.dashdoc;

public class DdCommon {

	final static public String Title_Delim_Parse = ":";
	final static public String Title_Delim_Format = " : ";

	public static String formatTitle(String t) {
		if (t.indexOf(Title_Delim_Parse) == -1) {
			return t.trim();
		}
		String ss[] = t.split(Title_Delim_Parse);
		String ret = null;
		for (int i = 0; i < ss.length; i++) {
			if (ret == null) {
				ret = ss[i].trim();
			} else {
				ret = ret + Title_Delim_Format + ss[i].trim();
			}
		}
		return ret;
	}

}
