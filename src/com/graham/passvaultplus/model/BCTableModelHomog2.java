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
public class BCTableModelHomog2 implements BCTableModel {

	final RecordFilter filter;
	PvpContext context;
	String currentType;
	//int currentTypeRowCount;
	List<String> fieldNamesToDisplay;
	
	List<FieldAndRecord> cacheData = null;
	
	public BCTableModelHomog2(final RecordFilter f, final PvpContext c) {
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
		
		cacheData = new ArrayList<FieldAndRecord>();
		
		getSingleTypeRows();
		
		int max = filter.getRecordCount();
		
		final FieldAndRecord dummy = new FieldAndRecord(null, null);
		
		for (int i = 0; i < max; i++) {
			final PvpRecord r = filter.getRecordAtIndex(i);
			//final Iterator iter = fieldNamesToDisplay.iterator();
			for (final String name: fieldNamesToDisplay) {
				//final String name = (String)iter.next();
				final String val = r.getCustomField(name);
				if (val != null && val.length() > 0) {
					cacheData.add(new FieldAndRecord(r, name));
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
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		final FieldAndRecord fr = (FieldAndRecord)getCacheData().get(rowIndex);
		
		if (columnIndex == 0) {
			return fr.fieldName;
		} else if (columnIndex == 1) {
			if (fr.record == null) {
				return "";
			}
			return fr.record.getCustomField(fr.fieldName);
		}

		return null;
	}
	
	public PvpRecord getRecordAtRow(final int rowIndex) {
		final FieldAndRecord fr = (FieldAndRecord)getCacheData().get(rowIndex);
		return fr.record;
	}
	
	public void getSingleTypeRows() {
		if (filter.getRecordCount() == 0) {
			return;
		}
		
		PvpRecord r = filter.getRecordAtIndex(0);
		
		if (r.getType().getName().equals(currentType)) {
			return;// currentTypeRowCount;
		}
		currentType = r.getType().getName();

		List<PvpField> typeFields = r.getType().getFields();
		fieldNamesToDisplay = new ArrayList<String>();
		//currentTypeRowCount = 0;
		
		for (final PvpField f : typeFields) {
			//RtField f = (RtField)typeFields.get(i);
			if (f.getName().equals(PvpField.USR_CREATION_DATE)) {
				
			} else if (f.getName().equals(PvpField.USR_MODIFICATION_DATE)) {
				
			} else {
				fieldNamesToDisplay.add(f.getName());
				//currentTypeRowCount++;
			}
		}
		
		//return currentTypeRowCount;
	}
	
}
