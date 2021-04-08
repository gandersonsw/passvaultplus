/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
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
 * Field       Value
 * Type        Address
 * Name        Joe
 * Email       joe@email.com
 *
 * Type        Account
 * Name        Amazon
 * Username    jsmith
 */
public class BCTableModelHeterVert implements BCTableModel {

	final RecordFilter filter;
	final PvpContext context;
	List<PvpField> fieldsToDisplay;
	
	public BCTableModelHeterVert(final RecordFilter f, final PvpContext contextParam) {
		filter = f;
		context = contextParam;
	}
	
	public void flushCache() {
		fieldsToDisplay = null;
	}
	
	public int getColumnCount() {
		return 2;
	}
	
	public int getRowCount() {
		return filter.getRecordCount() * getFieldsToDisplay().size();
	}

	public String getColumnName(int columnIndex) {
		return columnIndex == 0 ? "Field" : "Value";
	}

	public Object getValueAt(int rowIndex, int columnIndex, boolean returnSecretRealValue) {
		return getValueAt(rowIndex, columnIndex);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		int fieldIndex = rowIndex % getFieldsToDisplay().size();
		if (columnIndex == 0) {
			return getFieldsToDisplay().get(fieldIndex).getName();
		} else if (columnIndex == 1) {
			PvpRecord rec = getRecordAtRow(rowIndex);
			return rec.getAnyFieldLocalized(getFieldsToDisplay().get(fieldIndex));
		}
		return null;
	}
	
	public PvpRecord getRecordAtRow(final int rowIndex) {
		return filter.getRecordAtIndex(rowIndex / getFieldsToDisplay().size());
	}

	private List<PvpField> getFieldsToDisplay() {
		if (fieldsToDisplay != null) {
			return fieldsToDisplay;
		}

		fieldsToDisplay = new ArrayList<>();
		fieldsToDisplay.add(PvpField.CF_TYPE);
		fieldsToDisplay.add(PvpField.CF_CATEGORY);
		fieldsToDisplay.add(PvpField.CF_VIRTUAL_SUMMARY);

		Set<Integer> excludeCfids = new HashSet<>();
		excludeCfids.add(PvpField.CF_TYPE.getCoreFieldId());
		excludeCfids.add(PvpField.CF_CATEGORY.getCoreFieldId());
		excludeCfids.add(PvpField.CF_VIRTUAL_SUMMARY.getCoreFieldId());

		context.prefs.getRecordListViewOptions().addCommonFields(fieldsToDisplay, excludeCfids);

		fieldsToDisplay.add(PvpField.CF_VIRTUAL_PLACE_HOLDER);
		return fieldsToDisplay;
	}

	public boolean isVertModel() {
		return true;
	}
	
}
