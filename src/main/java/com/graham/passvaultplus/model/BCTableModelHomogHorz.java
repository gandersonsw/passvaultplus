/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.ArrayList;
import java.util.List;

import com.graham.passvaultplus.AppUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.model.core.PvpType;

/**
 * All the same PvpType. Example:
 *
 * Name       Email
 * Joe        joe@email.com
 * Jane       jane@email.com
 *
 */
public class BCTableModelHomogHorz implements BCTableModel {

	final RecordFilter filter;
	final PvpContext context;
	List<PvpField> fieldsToDisplay;

	public BCTableModelHomogHorz(final RecordFilter f, final PvpContext c) {
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
		PvpField field = getFieldsToDisplay().get(columnIndex);
		return field.getName();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return getValueAt(rowIndex, columnIndex, false);
	}

	public Object getValueAt(int rowIndex, int columnIndex, boolean returnSecretRealValue) {
		PvpRecord rec = filter.getRecordAtIndex(rowIndex);
		PvpField field = getFieldsToDisplay().get(columnIndex);

		if (!returnSecretRealValue && field.isClassificationSecret()) {
			return "******";
		}
		return rec.getAnyField(field);
	}

	public PvpRecord getRecordAtRow(final int rowIndex) {
		return filter.getRecordAtIndex(rowIndex);
	}

	private List<PvpField> getFieldsToDisplay() {
		if (fieldsToDisplay != null) {
			return fieldsToDisplay;
		}

		fieldsToDisplay = new ArrayList<>();

		if (filter.getRecordCount() == 0) {
		//	fieldsToDisplay.add("Field");
		//	fieldsToDisplay.add("Value");
			return fieldsToDisplay;
		}

		PvpRecord r = filter.getRecordAtIndex(0);
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
				String val = filter.getRecordAtIndex(i).getAnyField(fieldsToDisplay.get(fi));
				if (AppUtil.stringNotEmpty(val)) {
					valExists[fi] = true;
				}
			}
		}

		List<PvpField> fieldsToDisplayFiltered = new ArrayList<>();
		for (int i = 0; i < fieldsToDisplay.size(); i++) {
			if (valExists[i]) {
				fieldsToDisplayFiltered.add(fieldsToDisplay.get(i));
			}
		}
		fieldsToDisplay = fieldsToDisplayFiltered;

		return fieldsToDisplay;
	}
}
