package com.graham.swingui.dashdoc.model;

import com.graham.swingui.dashdoc.DdCommon;

public class DdSubsection extends DdContainer {
	String title;

	public DdSubsection(String t) {
		title = DdCommon.formatTitle(t);
	}

	public boolean supportsAdd(DdPart part) {
		return part instanceof DdLink || part instanceof DdText;
	}

	public String getTitle() {
		return title;
	}

}
