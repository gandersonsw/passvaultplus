/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.ArrayList;
import java.util.List;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;

import com.graham.passvaultplus.model.search.SearchRecord;

/**
 * All the same PvpType. Example:
 *
 * Field       Value
 * Name        Joe
 * Email       joe@email.com
 *
 * Name        Jane
 * Email       jane@email.com
 *
 */
public class BCTableModelHomogVert extends BCTableModelVert {

	List<PvpField> fieldsToDisplay;

	public BCTableModelHomogVert(final RecordFilter f, final PvpContext c) {
		super(f, c);
	}
	
	List<FieldAndRecord> getCacheData() {
		if (cacheData != null) {
			return cacheData;
		}
		
		cacheData = new ArrayList<>();
		
		computeFieldsToDisplay();
		
		int max = filter.getRecordCount();
		
		final FieldAndRecord dummy = new FieldAndRecord(null, null);
		
		for (int i = 0; i < max; i++) {
			final SearchRecord sr = filter.getRecordAtIndex(i);
			for (final PvpField field: fieldsToDisplay) {
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
	
	private void computeFieldsToDisplay() {
		if (filter.getRecordCount() == 0) {
			return;
		}
		
		PvpRecord r = filter.getRecordAtIndex(0).record;

		List<PvpField> typeFields = r.getType().getFields();
		fieldsToDisplay = new ArrayList<>();
		
		for (final PvpField f : typeFields) {
			if (context.prefs.getRecordListViewOptions().shouldShowField(f)) {
				fieldsToDisplay.add(f);
			}
		}

		context.prefs.getRecordListViewOptions().addCommonFields(fieldsToDisplay);
	}

}
