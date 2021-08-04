/* Copyright (C) 2021 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.ArrayList;
import java.util.List;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;

import com.graham.passvaultplus.model.search.SearchRecord;

public abstract class BCTableModelVert implements BCTableModel {

	final RecordFilter filter;
	final PvpContext context;
	List<FieldAndRecord> cacheData = null;

	int colNumMatch;
	int colNumField;
	int colNumValue;
	
	public BCTableModelVert(final RecordFilter f, final PvpContext c) {
		filter = f;
		context = c;
	}
	
	public void flushCache() {
		cacheData = null;
	}
	
	abstract List<FieldAndRecord> getCacheData();
	
	public int getColumnCount() {
		getCacheData();
		return colNumValue + 1;
	}
	
	public int getRowCount() {
		return getCacheData().size();
	}

	public String getColumnName(int columnIndex) {
		getCacheData();
		if (columnIndex == colNumMatch) {
			return "";
		}
		if (columnIndex == colNumField) {
			return "Field";
		}
		if (columnIndex == colNumValue) {
			return "Value";
		}
		return "??";
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return getValueAt(rowIndex, columnIndex, false);
	}

	public Object getValueAt(int rowIndex, int columnIndex, boolean returnSecretRealValue) {
		final FieldAndRecord fr = getCacheData().get(rowIndex);
		
		if (columnIndex == colNumMatch) {
			if (fr.sr == null) {
				return "";
			}
			return Integer.toString(fr.sr.match);
		} else if (columnIndex == colNumField) {
			return fr.getName();
		} else if (columnIndex == colNumValue) {
			if (fr.sr == null) {
				return "";
			}
			if (!returnSecretRealValue && fr.field.isClassificationSecret()) {
				return "******";
			}
			return fr.sr.record.getAnyFieldLocalized(fr.field);
		}

		return null;
	}
	
	public PvpRecord getRecordAtRow(final int rowIndex) {
		final FieldAndRecord fr = getCacheData().get(rowIndex);
		return fr.sr == null ? null : fr.sr.record;
	}

	public boolean isVertModel() {
		return true;
	}

}
