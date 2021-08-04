/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;

/**
 * Different PvpTypes. Example:
 *
 * Type        Category     Summary      Full
 * Address     Work         Joe          Joe joe@email.com
 * Account     Home         Amazon       Amazon jsmith
 *
 */
public class BCTableModelHeterHorz extends BCTableModelHorz {

	public BCTableModelHeterHorz(final RecordFilter f, final PvpContext c) {
		super(f, c);
	}

	List<PvpField> getFieldsToDisplay() {
		if (fieldsToDisplay != null) {
			return fieldsToDisplay;
		}

		fieldsToDisplay = new ArrayList<>();
		if (!filter.isAllTheSameMatch()) {
			fieldsToDisplay.add(PvpField.CF_SEARCH_MATCH);
		}
		fieldsToDisplay.add(PvpField.CF_TYPE);
		fieldsToDisplay.add(PvpField.CF_CATEGORY);
		fieldsToDisplay.add(PvpField.CF_VIRTUAL_SUMMARY);
		fieldsToDisplay.add(PvpField.CF_VIRTUAL_FULL);

		Set<Integer> excludeCfids = new HashSet<>();
		excludeCfids.add(PvpField.CF_TYPE.getCoreFieldId());
		excludeCfids.add(PvpField.CF_CATEGORY.getCoreFieldId());
		excludeCfids.add(PvpField.CF_VIRTUAL_SUMMARY.getCoreFieldId());
		excludeCfids.add(PvpField.CF_VIRTUAL_FULL.getCoreFieldId());

		context.prefs.getRecordListViewOptions().addCommonFields(fieldsToDisplay, excludeCfids);

		return fieldsToDisplay;
	}

}
