/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.util.ArrayList;
import java.util.List;

public class PvpType {
	
	static public final String FILTER_ALL_TYPES = "[All]";

	private String name;
	private String toStringCode; // what field(s) the toString method should use
	private List<PvpField> fields = new ArrayList<PvpField>();
	
	public String getName() {
		return name;
	}
	
	public void setName(final String s) {
		name = s;
	}
	
	public void addField(PvpField f) {
		fields.add(f);
	}
	
	public List<PvpField> getFields() {
		return fields;
	}
	
	public void setToStringCode(final String s) {
		toStringCode = s;
	}
	
	public String getToStringCode() {
		return toStringCode;
	}
	
	public String toString() {
		return name;
	}
	
}
