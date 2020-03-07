/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.dashdoc.model;

import com.graham.dashdoc.DdCommon;

public class DdSection extends DdContainer {
	public final String title;

	public DdSection(String t) {
		title = DdCommon.formatTitle(t);
	}

	public boolean supportsAdd(DdPart part) {
		return part instanceof DdText || part instanceof DdLink || part instanceof DdText || part instanceof DdSubsection;
	}

	@Override
	public String getTitle() {
		return title;
	}
}
