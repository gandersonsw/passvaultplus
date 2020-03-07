/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.dashdoc;

import java.io.BufferedReader;
import java.io.IOException;

import com.graham.util.AppUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.dashdoc.model.DashDoc;
import com.graham.dashdoc.model.DdLink;
import com.graham.dashdoc.model.DdPart;
import com.graham.dashdoc.model.DdSection;
import com.graham.dashdoc.model.DdSubsection;
import com.graham.dashdoc.model.DdText;

/**
 * Dash Doc Parser
 */
public class DdParser implements PvpContext.IOFunction<DashDoc> {

	static final char DASH = '-';
	static final char COMMENT = '#';
	static final String SectionDelim = "- - - -";
	static final String SubSectionDelim = "- -";
	static final String Control_Link = "- ";
	static final String Control_Newline = "-N";

	enum LineType {
		normal,
		comment,
		section,
		subsection,
		link,
		newline,
		error,
		nothing,
		naturalNewline
	}

	PvpContext context;
	boolean keepNewlines = false;

	public DdParser(PvpContext c) {
		context = c;
	}

	@Override
	public DashDoc apply(BufferedReader bufR) throws IOException {
		String line;
		DashDoc dd = new DashDoc();

		while ((line = bufR.readLine()) != null) {
			DdPart part = null;
			switch (getLineType(line)) {
				case normal:
					part = new DdText(keepNewlines ? line + "\n" : line);
					break;
				case comment:
					// do nothing with comments for now
					break;
				case section:
					dd.addSection(parseSection(line));
					break;
				case subsection:
					part = parseSubsection(line);
					break;
				case link:
					part = parseLink(line);
					break;
				case newline:
					part = parseNewline(line);
					break;
				case error:
					context.ui.notifyWarning("DdParser: line is in error: " + line);
					break;
				case nothing:
					// this is a blank line. these can be ignored
					break;
				case naturalNewline:
					part = new DdText("\n");
					break;
			}
			if (part != null) {
				if (!dd.addPart(part)) {
					context.ui.notifyWarning("DdParser: found line without a section: " + line);
				}
			}
		}
		dd.postLoadCleanup(null);
		return dd;
	}

	private LineType getLineType(String line) {
		String t = line.trim();
		if (t.length() == 0) {
			if (keepNewlines) {
				return LineType.naturalNewline;
			} else {
				return LineType.nothing;
			}
		}
		if (t.charAt(0) == DASH) {
			if (t.startsWith(SectionDelim)) {
				return LineType.section;
			}
			if (t.startsWith(SubSectionDelim)) {
				return LineType.subsection;
			}
			if (t.startsWith(Control_Newline)) {
				return LineType.newline;
			}
			if (t.startsWith(Control_Link)) {
				return LineType.link;
			}
			return LineType.error;
		} else if (t.charAt(0) == COMMENT) {
			return LineType.comment;
		}
		return LineType.normal;
	}

	private String getTitle(String line, String delim) {
		line = line.trim();
		line = line.substring(delim.length());
		if (line.endsWith(delim)) {
			line = line.substring(0, line.length() - delim.length());
		}
		return line.trim();
	}

	private DdSection parseSection(String line) {
		return new DdSection(getTitle(line, SectionDelim));
	}

	private DdSubsection parseSubsection(String line) {
		return new DdSubsection(getTitle(line, SubSectionDelim));
	}

	private DdLink parseLink(String line) {
		int tCount = 0;
		while (line.charAt(tCount) == '\t') {
			tCount++;
		}
		line = line.trim();
		line = line.substring(1);
		line = line.trim();
		return new DdLink(line, tCount);
	}

	private DdPart parseNewline(String line) {
		line = line.trim();
		String option = getControlOption(line);
		if (option.equals("ON")) {
			keepNewlines = true;
		} else if (option.equals("OFF")) {
			keepNewlines = false;
		} else {
			Integer i = AppUtil.tryParseInt(option);
			return new DdText(AppUtil.repeatString("\n", i));
		}
		return null;
	}

	private String getControlOption(String trimLine) {
		return trimLine.substring(2).trim();
	}
}
