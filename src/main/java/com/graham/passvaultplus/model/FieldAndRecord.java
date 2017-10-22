/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;

public class FieldAndRecord {
	public PvpField field;
	public PvpRecord record;
	
	public FieldAndRecord(final PvpRecord r, final PvpField f) {
		record = r;
		field = f;
	}
	
	public String getName() {
		if (field == null) {
			return "";
		}
		return field.getName();
	}
}
