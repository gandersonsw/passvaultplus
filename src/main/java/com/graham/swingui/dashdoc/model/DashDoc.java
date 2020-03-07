/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.swingui.dashdoc.model;

public class DashDoc extends DdContainer {

	public void addSection(DdSection s) {
		addPartToThis(s);
	}

	@Override
	public boolean supportsAdd(DdPart part) {
		return part instanceof DdSection;
	}

	public DdSection getStartingSection() {
		return (DdSection)parts.get(0);
	}

	public DdSection getSection(String t) {
		for (DdPart p : parts) {
			DdSection s = (DdSection)p;
			if (s.getTitle().equals(t)) {
				return s;
			}
		}
		return null;
	}

	@Override
	public String getTitle() {
		return null;
	}

}
