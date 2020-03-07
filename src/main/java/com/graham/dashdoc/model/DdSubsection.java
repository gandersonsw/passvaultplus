/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.dashdoc.model;

import com.graham.dashdoc.DdCommon;

public class DdSubsection extends DdContainer {
	String title;

	public DdSubsection(String t) {
		title = DdCommon.formatTitle(t);
	}

	@Override
	public boolean supportsAdd(DdPart part) {
		return part instanceof DdLink || part instanceof DdText;
	}

	@Override
	public String getTitle() {
		return title;
	}

}
