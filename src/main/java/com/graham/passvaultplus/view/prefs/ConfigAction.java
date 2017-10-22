/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

public enum ConfigAction {
	Create("Create New Database", "Create"),
	Open("Open Existing Database", "Open"),
	Change("Change Database Options", "Save");
	
	String displayValue;
	String buttonLabel;
	
	ConfigAction(final String dv, String bl) {
		displayValue = dv;
		buttonLabel = bl;
	}
	
	public String getButtonLabel() {
		return buttonLabel;
	}
	
	@Override
	public String toString() {
		return displayValue;
	}
}
