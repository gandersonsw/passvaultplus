/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * General Utility - very generic class name for utility methods that don't have a grouping yet.
 */
public class GenUtil {

	public static String getExceptionStackTrace(final Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	public static String dumpInputStreamToString(InputStream s) throws IOException {
		byte b[] = new byte[1024];
		int bytes;
		StringBuilder sb = new StringBuilder();
		while ((bytes = s.read(b)) > 0) {
			String str = new String(b, 0, bytes);
			sb.append(str);
		}
		return sb.toString();
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
	
	public static boolean equalsWithNullFalse(final Object obj1, final Object obj2) {
		if (obj1 == null || obj2 == null) {
			return false;
		}
		return obj1.equals(obj2);
	}

}
