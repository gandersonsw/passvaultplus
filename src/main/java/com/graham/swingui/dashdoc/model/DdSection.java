package com.graham.swingui.dashdoc.model;

import com.graham.swingui.dashdoc.DdCommon;

public class DdSection extends DdContainer {
	public final String title;

	public DdSection(String t) {
		title = DdCommon.formatTitle(t);
	}

	public boolean supportsAdd(DdPart part) {
		return part instanceof DdText || part instanceof DdLink || part instanceof DdText || part instanceof DdSubsection;
	}

	public String getTitle() {
		return title;
	}
}
