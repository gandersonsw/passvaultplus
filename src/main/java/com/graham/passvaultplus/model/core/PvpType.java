/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.util.ArrayList;
import java.util.List;

public class PvpType {
	
	static public final String FILTER_ALL_TYPES = "[All]";

	private String name;
	private String toStringCode; // what field(s) the toString method should use
	private String fullFormat; // what this record should look like when formatted. May be null
	private PvpRecordFormatter fullFormatter;
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
	
	public PvpField getField(final String fieldName) {
		for (PvpField f : fields) {
			if (f.getName().equals(fieldName)) {
				return f;
			}
		}
		if (PvpField.USR_NOTES.equals(fieldName)) {
			// The notes field is different because it is always present.
			return new PvpField(PvpField.USR_NOTES, PvpField.TYPE_STRING);
		}
		return null;
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
	
	public void setFullFormat(final String s) {
		fullFormat = s;
		fullFormatter = null;
	}
	
	public String getFullFormat() {
		return fullFormat;
	}
	
	public PvpRecordFormatter getFullFormatter() {
		if (fullFormatter == null) {
			fullFormatter = new PvpRecordFormatter(fullFormat);
		}
		return fullFormatter;
	}
	
	public String toString() {
		return name;
	}
	
	public static boolean sameType(final PvpType t1, final PvpType t2) {
		if (t1 == null || t2 == null) {
			return false;
		}
		if (t1.getName() == null || t2.getName() == null) {
			return false;
		}
		return t1.getName().equals(t2.getName());
	}
	
	public static boolean sameType(final PvpType t1, final String t2) {
		if (t1 == null) {
			return false;
		}
		if (t1.getName() == null || t2 == null) {
			return false;
		}
		return t1.getName().equals(t2);
	}
	
	
	
}
