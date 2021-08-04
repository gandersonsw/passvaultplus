/* Copyright (C) 2021 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.ArrayList;
import java.util.List;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.model.core.PvpType;
import com.graham.util.StringUtil;

import com.graham.passvaultplus.model.search.SearchRecord;

public abstract class BCTableModelHorz implements BCTableModel {

	final RecordFilter filter;
	final PvpContext context;
	List<PvpField> fieldsToDisplay;

	public BCTableModelHorz(final RecordFilter f, final PvpContext c) {
		filter = f;
		context = c;
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
		PvpField f = getFieldsToDisplay().get(columnIndex);
		if (f.getCoreFieldId() == PvpField.CFID_SEARCH_MATCH) {
			return "";
		}
		return f.getName();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return getValueAt(rowIndex, columnIndex, false);
	}

	public Object getValueAt(int rowIndex, int columnIndex, boolean returnSecretRealValue) {
		SearchRecord sr = filter.getRecordAtIndex(rowIndex);
		PvpField field = getFieldsToDisplay().get(columnIndex);
		
		if (field.getCoreFieldId() == PvpField.CFID_SEARCH_MATCH) {
			return Integer.toString(sr.match);
		}

		if (!returnSecretRealValue && field.isClassificationSecret()) {
			return "******";
		}
		return sr.record.getAnyFieldLocalized(field);
	}

	public PvpRecord getRecordAtRow(final int rowIndex) {
		return filter.getRecordAtIndex(rowIndex).record;
	}

	abstract List<PvpField> getFieldsToDisplay();

	public boolean isVertModel() {
		return false;
	}
}
