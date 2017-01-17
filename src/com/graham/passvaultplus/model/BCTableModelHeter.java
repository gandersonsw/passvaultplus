/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;

/**
 * Different types.
 */
public class BCTableModelHeter implements BCTableModel {

	final RecordFilter filter;
	final PvpContext context;
	
	public BCTableModelHeter(final RecordFilter f, final PvpContext contextParam) {
		filter = f;
		context = contextParam;
	}
	
	public void flushCache() {
		// nothing to do
	}
	
	public int getColumnCount() {
		return 2;
	}
	
	public int getRowCount() {
		return filter.getRecordCount() * 4;
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		int fieldIndex = rowIndex % 4;
		if (columnIndex == 0) {
			switch (fieldIndex) {
			case 0:
				return "Type";
			case 1:
				return "Category";
			case 2:
				return "Summary";
			case 3:
				return "";
			}
		} else if (columnIndex == 1) {
			PvpRecord rec = getRecordAtRow(rowIndex);
			switch (fieldIndex) {
			case 0:
				return rec.getType();
			case 1:
				if (rec.getCategory() == null) {
					return PvpRecord.NO_CATEGORY;
				}
				return rec.getCategory().getCustomField(PvpField.USR_CATEGORY_TITLE);
			case 2:
				return rec.toString();
			case 3:
				return "";
			}
		}

		return null;
	}
	
	public PvpRecord getRecordAtRow(final int rowIndex) {
		return filter.getRecordAtIndex(rowIndex / 4);
	}
	
}
