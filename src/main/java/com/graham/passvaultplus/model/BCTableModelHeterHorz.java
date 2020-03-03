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
public class BCTableModelHeterHorz implements BCTableModel {

	final RecordFilter filter;
	final PvpContext context;
	List<PvpField> fieldsToDisplay;

	public BCTableModelHeterHorz(final RecordFilter f, final PvpContext contextParam) {
		filter = f;
		context = contextParam;
	}

	public void flushCache() {
		fieldsToDisplay = null;
	}

	public int getColumnCount() {
		return getFieldsToDisplay().size();
	}

	public int getRowCount() {
		return filter.getRecordCount();
	}

	public String getColumnName(int columnIndex) {
		return getFieldsToDisplay().get(columnIndex).getName();
	}

	public Object getValueAt(int rowIndex, int columnIndex, boolean returnSecretRealValue) {
		return getValueAt(rowIndex, columnIndex);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		PvpRecord rec = filter.getRecordAtIndex(rowIndex);
		return rec.getAnyField(getFieldsToDisplay().get(columnIndex));
	}

	public PvpRecord getRecordAtRow(final int rowIndex) {
		return filter.getRecordAtIndex(rowIndex);
	}

	private List<PvpField> getFieldsToDisplay() {
		if (fieldsToDisplay != null) {
			return fieldsToDisplay;
		}

		fieldsToDisplay = new ArrayList<>();
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

	public boolean isVertModel() {
		return false;
	}
}
