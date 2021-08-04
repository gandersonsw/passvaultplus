/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;

import com.graham.passvaultplus.model.search.SearchRecord;

/**
 * Different PvpTypes. Example:
 *
 * Field       Value
 * Type        Address
 * Name        Joe
 * Email       joe@email.com
 *
 * Type        Account
 * Name        Amazon
 * Username    jsmith
 */
public class BCTableModelHeterVertDetailed extends BCTableModelVert {
	
	Map<String, List<PvpField>> fieldsToDisplayCache; //  = new HashMap<>();

	public BCTableModelHeterVertDetailed(final RecordFilter f, final PvpContext c) {
		super(f, c);
	}

	List<FieldAndRecord> getCacheData() {
		if (cacheData != null) {
			return cacheData;
		}

		//Map<String, List<PvpField>> fieldsToDisplay = new HashMap<>();
		fieldsToDisplayCache = new HashMap<>();
		cacheData = new ArrayList<>();

		int max = filter.getRecordCount();

		final FieldAndRecord dummy = new FieldAndRecord(null, null);

		for (int i = 0; i < max; i++) {
			final SearchRecord sr = filter.getRecordAtIndex(i);
			for (final PvpField field: getFieldsToDisplay(sr.record)) {
				final String val = sr.record.getAnyFieldLocalized(field);
				if (val != null && val.length() > 0) {
					cacheData.add(new FieldAndRecord(sr, field));
				}
			}
			cacheData.add(dummy);
		}

		if (filter.isAllTheSameMatch()) {
			colNumMatch = -1;
			colNumField = 0;
			colNumValue = 1;
		} else {
			colNumMatch = 0;
			colNumField = 1;
			colNumValue = 2;
		}
		
		return cacheData;
	}

	private List<PvpField> getFieldsToDisplay(final PvpRecord r) {
		String tName = r.getType().getName();
		if (fieldsToDisplayCache.containsKey(tName)) {
			return fieldsToDisplayCache.get(tName);
		}

		List<PvpField> typeFields = r.getType().getFields();
		List<PvpField> fieldsToDisplay = new ArrayList<>();

		for (final PvpField f : typeFields) {
			if (context.prefs.getRecordListViewOptions().shouldShowField(f)) {
				fieldsToDisplay.add(f);
			}
		}

		context.prefs.getRecordListViewOptions().addCommonFields(fieldsToDisplay);
		fieldsToDisplayCache.put(tName, fieldsToDisplay);
		return fieldsToDisplay;
	}

}