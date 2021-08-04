/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.search.SearchRecord;

public class FieldAndRecord {
	public PvpField field;
	public SearchRecord sr;
	
	public FieldAndRecord(final SearchRecord r, final PvpField f) {
		sr = r;
		field = f;
	}
	
	public String getName() {
		if (field == null) {
			return "";
		}
		return field.getName();
	}
}
