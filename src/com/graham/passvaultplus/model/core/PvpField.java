/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.util.ArrayList;
import java.util.List;

public class PvpField {

	// common for all types, these are the fields as they would appear as XML element names
	final static public String XML_CATEGORY = "category";
	final static public String XML_CREATION_DATE = "creation-date";
	final static public String XML_MODIFICATION_DATE = "modification-date";
	final static public String XML_NOTES = "notes";
	final static public String XML_TYPE = "type";
	
	// common for all types, these are the elements as they would be when loaded in Java, and also in the XML element type.field.name
	final static public String USR_CATEGORY = "Category"; // this is not the type, but the field name
	final static public String USR_CREATION_DATE = "Creation Date";
	final static public String USR_MODIFICATION_DATE = "Modification Date";
	final static public String USR_NOTES = "Notes";
	final static public String USR_TYPE = "Type";
	
	// for category type only
	final static public String USR_CATEGORY_TITLE = "Title";
	
	// classifications
	final static public String CLASSIFICATION_SECRET = "secret";
	
	// for PvpField.type
	final static public String TYPE_STRING = "String";
	final static public String TYPE_LONG_STRING = "Long String"; // multi-line string
	final static public String TYPE_DATE = "Date"; // May have a year, may have a time, may have seconds
	final static public String[] TYPES = new String[]{TYPE_STRING, TYPE_LONG_STRING, TYPE_DATE};
	
	private String name;
	private String type;
	private String classification; // may be null
	
	public PvpField(final String nameParam, final String typeParam) {
		name = nameParam;
		type = typeParam;
	}
	
	public PvpField(final String nameParam, final String typeParam, final String classificationParam) {
		name = nameParam;
		type = typeParam;
		classification = classificationParam;
	}

	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public String getClassification() {
		return classification;
	}
	
	public boolean isClassificationSecret() {
		return CLASSIFICATION_SECRET.equals(classification);
	}
	
	
	public void setName(String n) {
		name = n;
	}
	
	public void setType(String t) {
		type = t;
	}
	
	public void setClassification(String c) {
		classification = c;
	}
	
}
