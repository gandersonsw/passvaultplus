/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.ArrayList;
import java.util.List;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.model.core.PvpType;
import com.graham.util.StringUtil;

import com.graham.passvaultplus.model.search.SearchRecord;

/**
 * All the same PvpType. Example:
 *
 * Name       Email
 * Joe        joe@email.com
 * Jane       jane@email.com
 *
 */
public class BCTableModelHomogHorz extends BCTableModelHorz {

	public BCTableModelHomogHorz(final RecordFilter f, final PvpContext c) {
		super(f, c);
	}

	List<PvpField> getFieldsToDisplay() {
		if (fieldsToDisplay != null) {
			return fieldsToDisplay;
		}

		fieldsToDisplay = new ArrayList<>();

		if (filter.getRecordCount() == 0) {
			return fieldsToDisplay;
		}
		
		PvpRecord r = filter.getRecordAtIndex(0).record;
		PvpType currentType = r.getType();

		List<PvpField> typeFields = currentType.getFields();
		for (final PvpField f : typeFields) {
			if (context.prefs.getRecordListViewOptions().shouldShowField(f)) {
				fieldsToDisplay.add(f);
			}
		}

		context.prefs.getRecordListViewOptions().addCommonFields(fieldsToDisplay);

		int count = filter.getRecordCount();
		boolean valExists[] = new boolean[fieldsToDisplay.size()];
		for (int i = 0; i < count; i++) {
			for (int fi = 0; fi < fieldsToDisplay.size(); fi++) {
				String val = filter.getRecordAtIndex(i).record.getAnyFieldLocalized(fieldsToDisplay.get(fi));
				if (StringUtil.stringNotEmpty(val)) {
					valExists[fi] = true;
				}
			}
		}

		List<PvpField> fieldsToDisplayFiltered = new ArrayList<>();
		if (!filter.isAllTheSameMatch()) {
			fieldsToDisplayFiltered.add(PvpField.CF_SEARCH_MATCH);
		}
		
		for (int i = 0; i < fieldsToDisplay.size(); i++) {
			if (valExists[i]) {
				fieldsToDisplayFiltered.add(fieldsToDisplay.get(i));
			}
		}
		fieldsToDisplay = fieldsToDisplayFiltered;

		return fieldsToDisplay;
	}

}
