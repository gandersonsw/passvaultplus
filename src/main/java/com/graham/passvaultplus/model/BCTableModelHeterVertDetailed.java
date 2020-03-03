/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;

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
public class BCTableModelHeterVertDetailed implements BCTableModel {

	final RecordFilter filter;
	final PvpContext context;
	List<FieldAndRecord> cacheData = null;

	public BCTableModelHeterVertDetailed(final RecordFilter f, final PvpContext c) {
		filter = f;
		context = c;
	}

	public void flushCache() {
		cacheData = null;
	}

	private List<FieldAndRecord> getCacheData() {
		if (cacheData != null) {
			return cacheData;
		}

		Map<String, List<PvpField>> fieldsToDisplay = new HashMap<>();
		cacheData = new ArrayList<>();

		int max = filter.getRecordCount();

		final FieldAndRecord dummy = new FieldAndRecord(null, null);

		for (int i = 0; i < max; i++) {
			final PvpRecord r = filter.getRecordAtIndex(i);
			for (final PvpField field: getFieldsToDisplay(r, fieldsToDisplay)) {
				final String val = r.getAnyField(field);
				if (val != null && val.length() > 0) {
					cacheData.add(new FieldAndRecord(r, field));
				}
			}
			cacheData.add(dummy);
		}

		return cacheData;
	}

	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		return getCacheData().size();
	}

	public String getColumnName(int columnIndex) {
		return columnIndex == 0 ? "Field" : "Value";
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return getValueAt(rowIndex, columnIndex, false);
	}

	public Object getValueAt(int rowIndex, int columnIndex, boolean returnSecretRealValue) {
		final FieldAndRecord fr = getCacheData().get(rowIndex);

		if (columnIndex == 0) {
			return fr.getName();
		} else if (columnIndex == 1) {
			if (fr.record == null) {
				return "";
			}
			if (!returnSecretRealValue && fr.field.isClassificationSecret()) {
				return "******";
			}
			return fr.record.getAnyField(fr.field);
		}

		return null;
	}

	public PvpRecord getRecordAtRow(final int rowIndex) {
		final FieldAndRecord fr = getCacheData().get(rowIndex);
		return fr.record;
	}

	private List<PvpField> getFieldsToDisplay(final PvpRecord r, Map<String, List<PvpField>> fieldsToDisplayCache) {
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

	public boolean isVertModel() {
		return true;
	}

}