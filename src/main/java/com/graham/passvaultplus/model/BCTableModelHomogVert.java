/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.ArrayList;
import java.util.List;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;

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
public class BCTableModelHomogVert implements BCTableModel {

	final RecordFilter filter;
	final PvpContext context;
	List<PvpField> fieldsToDisplay;
	List<FieldAndRecord> cacheData = null;
	
	public BCTableModelHomogVert(final RecordFilter f, final PvpContext c) {
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
		
		cacheData = new ArrayList<>();
		
		computeFieldsToDisplay();
		
		int max = filter.getRecordCount();
		
		final FieldAndRecord dummy = new FieldAndRecord(null, null);
		
		for (int i = 0; i < max; i++) {
			final PvpRecord r = filter.getRecordAtIndex(i);
			for (final PvpField field: fieldsToDisplay) {
				final String val = r.getAnyFieldLocalized(field);
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
			return fr.record.getAnyFieldLocalized(fr.field);
		}

		return null;
	}
	
	public PvpRecord getRecordAtRow(final int rowIndex) {
		final FieldAndRecord fr = getCacheData().get(rowIndex);
		return fr.record;
	}
	
	private void computeFieldsToDisplay() {
		if (filter.getRecordCount() == 0) {
			return;
		}
		
		PvpRecord r = filter.getRecordAtIndex(0);

		List<PvpField> typeFields = r.getType().getFields();
		fieldsToDisplay = new ArrayList<>();
		
		for (final PvpField f : typeFields) {
			if (context.prefs.getRecordListViewOptions().shouldShowField(f)) {
				fieldsToDisplay.add(f);
			}
		}

		context.prefs.getRecordListViewOptions().addCommonFields(fieldsToDisplay);
	}

	public boolean isVertModel() {
		return false;
	}

}
