/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.util.ArrayList;

import com.graham.passvaultplus.PvpContextUI;
import com.graham.util.StringUtil;

/**
 * Format the record in its default human readable plain-text format.
 */
public class PvpRecordFormatter {

	ArrayList<FormatPart> formatParts;

	interface FormatPart {
		String format(final PvpRecord r);
	}

	static class ConstantPart implements FormatPart {
		private String text;
		public ConstantPart(final String textParam) {
			text = StringUtil.replaceAll(textParam, "\\n", "\n");
		}
		public String format(final PvpRecord r) {
			return text;
		}
	}

	static class FieldPart implements FormatPart {
		private String fieldName;
		public FieldPart(final String fieldNameParam) {
			fieldName = fieldNameParam;
		}
		public String format(final PvpRecord r) {
			return r.getCustomField(fieldName);
		}
	}

	public PvpRecordFormatter(final String format) {
		if (format != null) {
			try {
				parseFormat(format);
			} catch (Exception e) {
				PvpContextUI.getActiveUI().notifyWarning("PvpRecordFormatter: format=" + format, e);
				formatParts = new ArrayList<>();
				formatParts.add(new ConstantPart("format was bad: " + format));
			}
		}
	}

	private void parseFormat(final String format) {
		formatParts = new ArrayList<>();

		int currentIndex = 0;
		boolean doneProcessing = false;

		while (!doneProcessing) {
			int nextBracketStart = format.indexOf('[', currentIndex);
			int nextBracketEnd = -1;
			if (nextBracketStart > -1) {
				nextBracketEnd = format.indexOf(']', nextBracketStart);
			}

			if (nextBracketStart > -1 && nextBracketEnd > -1) {
				if (currentIndex < nextBracketStart) {
					formatParts.add(new ConstantPart(format.substring(currentIndex, nextBracketStart)));
				}
				formatParts.add(new FieldPart(format.substring(nextBracketStart + 1, nextBracketEnd)));
				currentIndex = nextBracketEnd + 1;
			} else {
				if (currentIndex < format.length()) {
					formatParts.add(new ConstantPart(format.substring(currentIndex, format.length())));
				}
				doneProcessing = true;
			}
		}
	}

	public String format(final PvpRecord r) {
		if (formatParts == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (FormatPart part : formatParts) {
			sb.append(part.format(r));
		}

		return sb.toString();
	}

}
