/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.ArrayList;
import java.util.List;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;

/**
 * All the same type.
 */
public class BCTableModelHomog implements BCTableModel {

	final RecordFilter filter;
	PvpContext context;
	String currentType;
	int currentTypeRowCount;
	List<String> fieldNamesToDisplay;
	
	public BCTableModelHomog(final RecordFilter f, final PvpContext c) {
		filter = f;
		context = c;
	}
	
	public void flushCache() {
		// nothign to do
	}
	
	public int getColumnCount() {
		return 2;
	}
	
	public int getRowCount() {
		return filter.getRecordCount() * (getSingleTypeRows() + 1);
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		int fieldIndex = rowIndex % (getSingleTypeRows() + 1);
		if (fieldIndex >= getSingleTypeRows()) {
			return "";
		}
		String fieldName = (String)fieldNamesToDisplay.get(fieldIndex);
		
		if (columnIndex == 0) {
			return fieldName;
		} else if (columnIndex == 1) {
			PvpRecord rec = getRecordAtRow(rowIndex);
			return rec.getCustomField(fieldName);
		}

		return null;
	}
	
	public PvpRecord getRecordAtRow(final int rowIndex) {
		return filter.getRecordAtIndex(rowIndex / (getSingleTypeRows() + 1));
	}
	
	public int getSingleTypeRows() {
		if (filter.getRecordCount() == 0) {
			return 0;
		}
		
		PvpRecord r = filter.getRecordAtIndex(0);
		
		if (r.getType().getName().equals(currentType)) {
			return currentTypeRowCount;
		}
		currentType = r.getType().getName();

		List<PvpField> typeFields = r.getType().getFields();
		fieldNamesToDisplay = new ArrayList<String>();
		currentTypeRowCount = 0;
		
		for (final PvpField f : typeFields) {
			if (f.getName().equals(PvpField.USR_CREATION_DATE)) {
				
			} else if (f.getName().equals(PvpField.USR_MODIFICATION_DATE)) {
				
			} else {
				fieldNamesToDisplay.add(f.getName());
				currentTypeRowCount++;
			}
		}
		
		return currentTypeRowCount;
	}
	
}
