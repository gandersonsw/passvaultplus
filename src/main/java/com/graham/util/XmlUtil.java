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
	 * Convert "first-name" to "First Name"
	 */
	public static String unmakeXMLName(final String txt) {
		String ret = txt.trim();
		int dashLoc;
		while ((dashLoc = ret.indexOf("-")) != -1) {
			//	String aa = ret.substring(0, dashLoc);
			//	String bb = ret.substring(dashLoc + 1, dashLoc + 2);
			//	String cc = ret.substring(dashLoc + 2);

			if (dashLoc + 2 > ret.length()) {
				ret = ret.substring(0, dashLoc) + " ";
			} else {
				ret = ret.substring(0, dashLoc) + " " + ret.substring(dashLoc + 1, dashLoc + 2).toUpperCase() + ret.substring(dashLoc + 2);
			}
		}
		ret = ret.substring(0,1).toUpperCase() + ret.substring(1);
		return ret;
	}

	/**
	 * Convert "First Name" to "first-name"
	 */
	public static String makeXMLName(final String txt) {
		String ret = txt;
		ret = StringUtil.replaceAll(ret," ","-");
		ret = ret.toLowerCase();
		return ret;
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
