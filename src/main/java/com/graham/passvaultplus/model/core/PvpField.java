/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

public class PvpField {

	// for category type only
	final static public String USR_CATEGORY_TITLE = "Title";
	
	// classifications
	final static public String CLASSIFICATION_SECRET = "secret";
	
	// for PvpField.type
	final static public String TYPE_STRING = "String";
	final static public String TYPE_LONG_STRING = "Long String"; // multi-line string
	final static public String TYPE_DATE = "Date"; // May have a year, may have a time, may have seconds
	final static public String[] TYPES = new String[]{TYPE_STRING, TYPE_LONG_STRING, TYPE_DATE};

	// Core Field IDs - these are not guaranteed to stay the same over time - so they should not be persisted
	final static public int FIRST_CFID = 901;
	final static public int CFID_CATEGORY = 901;
	final static public int CFID_CREATION_DATE = 902;
	final static public int CFID_MODIFICATION_DATE = 903;
	final static public int CFID_NOTES = 904;
	final static public int CFID_TYPE = 905;
	final static public int CFID_SUMMARY = 906;
	final static public int CFID_FULL = 907;
	final static public int CFID_PLACE_HOLDER = 908;
	final static public int CFID_INDENTITY = 998;
	final static public int CFID_UNDEF = 999;

	// Core Fields
	final static public PvpField CF_CATEGORY = new PvpField(CFID_CATEGORY, "Category", null);
	final static public PvpField CF_CREATION_DATE = new PvpField(CFID_CREATION_DATE, "Creation Date", TYPE_DATE);
	final static public PvpField CF_MODIFICATION_DATE = new PvpField(CFID_MODIFICATION_DATE, "Modification Date", TYPE_DATE);
	final static public PvpField CF_NOTES = new PvpField(CFID_NOTES, "Notes", TYPE_STRING);
	final static public PvpField CF_TYPE = new PvpField(CFID_TYPE, "Type", null);
	final static public PvpField CF_VIRTUAL_SUMMARY = new PvpField(CFID_SUMMARY, "Summary", null);
	final static public PvpField CF_VIRTUAL_FULL = new PvpField(CFID_FULL, "Full", null);
	final static public PvpField CF_VIRTUAL_PLACE_HOLDER = new PvpField(CFID_PLACE_HOLDER, "", null);
	final static public PvpField CF_INDENTITY = new PvpField(CFID_INDENTITY, "ID", null);

	private final int coreFieldId;
	private String name;
	private String type;
	private String classification; // may be null

	static public PvpField getCoreField(int cfid) {
		switch (cfid) {
			case CFID_CATEGORY:
				return CF_CATEGORY;
			case CFID_CREATION_DATE:
				return CF_CREATION_DATE;
			case CFID_MODIFICATION_DATE:
				return CF_MODIFICATION_DATE;
			case CFID_NOTES:
				return CF_NOTES;
			case CFID_TYPE:
				return CF_TYPE;
			case CFID_SUMMARY:
				return CF_VIRTUAL_SUMMARY;
			case CFID_FULL:
				return CF_VIRTUAL_FULL;
			case CFID_PLACE_HOLDER:
				return CF_VIRTUAL_PLACE_HOLDER;
		}
		return null;
	}

	public PvpField(final String nameParam, final String typeParam) {
		name = nameParam;
		type = typeParam;
		coreFieldId = CFID_UNDEF;
	}

	public PvpField(final String nameParam, final String typeParam, final String classificationParam) {
		name = nameParam;
		type = typeParam;
		classification = classificationParam;
		coreFieldId = CFID_UNDEF;
	}

	public PvpField(final int coreTypeIdParam, final String nameParam, final String typeParam) {
		name = nameParam;
		type = typeParam;
		coreFieldId = coreTypeIdParam;
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

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof PvpField) {
			return name.equals(((PvpField)obj).name);
		} else {
			throw new RuntimeException("Should call with PvpField");
		}
	}

	public int hashCode() {
		return name.hashCode();
	}

	public int getCoreFieldId() {
		return coreFieldId;
	}
	
}
