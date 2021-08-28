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
	private List<PvpField> fields = new ArrayList<>();
	
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
		if (PvpField.CF_NOTES.getName().equals(fieldName)) {
			// The notes field is different because it is always present.
			return PvpField.CF_NOTES;
		}
		return null;
	}
	
	public PvpField getFieldByXmlName(final String fieldName) {
		// TODO pull this ut and optimize ( in reader )
		for (PvpField f : fields) {
			if (f.getXmlName().equals(fieldName)) {
				return f;
			}
		}
		if (PvpField.CF_NOTES.getXmlName().equals(fieldName)) {
			// The notes field is different because it is always present.
			return PvpField.CF_NOTES;
		}
		
		if (PvpField.CF_CATEGORY.getXmlName().equals(fieldName)) {
			return PvpField.CF_CATEGORY;
		}
		
		if (PvpField.CF_CREATION_DATE.getXmlName().equals(fieldName)) {
			return PvpField.CF_CREATION_DATE;
		}
		
		if (PvpField.CF_MODIFICATION_DATE.getXmlName().equals(fieldName)) {
			return PvpField.CF_MODIFICATION_DATE;
		}
		
		if (PvpField.CF_TYPE.getXmlName().equals(fieldName)) {
			return PvpField.CF_TYPE;
		}
		
		if (PvpField.CF_ARCHIVED_FLAG.getXmlName().equals(fieldName)) {
			return PvpField.CF_ARCHIVED_FLAG;
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
