/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import com.graham.passvaultplus.model.core.PvpRecord;

public class FieldAndRecord {
	public String fieldName;
	public PvpRecord record;
	
	public FieldAndRecord(final PvpRecord r, final String n) {
		record = r;
		fieldName = n;
	}
}
