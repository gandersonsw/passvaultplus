/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.swingui.dashdoc.model;

public class DdText implements DdPart {
	public String text;

	private StringBuffer sb;

	public DdText(String t) {
		text = t;
	}

	@Override
	public boolean addPart(DdPart p) {
		if (p instanceof DdText) {
			DdText t = (DdText)p;
			if (t.text.length() == 0) {
				return true;
			}
			if (sb == null) {
				sb = text == null ? new StringBuffer() : new StringBuffer(text);
			}
			// if we are about to concat 2 strings with no whitespace, add a space.
			if (sb.length() > 0 && !Character.isWhitespace(sb.charAt(sb.length() - 1)) && !Character.isWhitespace(t.text.charAt(0))) {
				sb.append(' ');
			}
			sb.append(t.text);
			return true;
		}
		return false;
	}

	@Override
	public void postLoadCleanup(DdContainer parent) {
		if (sb != null) {
			text = sb.toString();
			sb = null;
		}
	}

	@Override
	public boolean supportsAdd(DdPart part)  {
		return part instanceof DdText;
	}

	@Override
	public String toString() {
		return text;
	}

}
