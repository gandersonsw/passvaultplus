/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.util;

import org.jdom2.Element;
import org.jdom2.Text;

public class XmlUtil {

	public static String makeXMLSafe(final String txt) {
		if (txt == null) {
			return "";
		}
		String ret = txt;
		ret = StringUtil.replaceAll(ret,"&","&amp;");
		ret = StringUtil.replaceAll(ret,"<","&lt;");
		ret = StringUtil.replaceAll(ret,">","&gt;");
		ret = StringUtil.replaceAll(ret,"\"","&quot;");
		return ret;
	}

	public static String unmakeXMLSafe(final String txt) {
		if (txt == null) {
			return "";
		}
		String ret = txt;
		ret = StringUtil.replaceAll(ret,"&lt;","<");
		ret = StringUtil.replaceAll(ret,"&gt;",">");
		ret = StringUtil.replaceAll(ret,"&quot;","\"");
		ret = StringUtil.replaceAll(ret,"&amp;","&");
		return ret;
	}

	/**
	 * Convert "First Name" to "first-name"
	 */
	public static String makeXMLName(final String txt) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < txt.length(); i++) {
			int cp = txt.codePointAt(i);
			if (Character.isSpaceChar(cp)) {
				if (i == 0) {
					ret.append("_");
				} else {
					ret.append("-");
				}
			} else if (Character.isAlphabetic(cp)) {
				ret.appendCodePoint(cp);
			} else if (Character.isDigit(cp)) {
				if (i == 0) {
					ret.append("_");
				}
				ret.appendCodePoint(cp);
			}
		}
		
		return ret.toString().toLowerCase();
	}

	public static void addOrSetContent(Element parent, String elementName, String text) {
		Element e = parent.getChild(elementName);
		Text t = new Text(text);
		if (e == null) {
			e = new Element(elementName);
			e.setContent(t);
			parent.addContent(e);
		} else {
			e.setContent(t);
		}
	}

}
