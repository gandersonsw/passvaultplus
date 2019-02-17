/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import javax.swing.table.AbstractTableModel;

import com.graham.passvaultplus.model.core.PvpRecord;

public class ListTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	RecordFilter filter;
	
	public ListTableModel(final RecordFilter filterParam) {
		filter = filterParam;
	}

	public int getColumnCount() {
		return filter.getCurrentModel().getColumnCount();
	}

	public int getRowCount() {
		return filter.getCurrentModel().getRowCount();
	}

	public Object getValueAt(int rowIndex, int columnIndex, boolean returnSecretRealValue) {
		return filter.getCurrentModel().getValueAt(rowIndex, columnIndex, returnSecretRealValue);
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
		return filter.getCurrentModel().getValueAt(rowIndex, columnIndex);
	}
	
	public PvpRecord getRecordAtRow(final int rowIndex) {
		return filter.getCurrentModel().getRecordAtRow(rowIndex);
	}
	
	public void filterUIChanged() {
		filter.filterUIChanged();
		fireTableStructureChanged();
	}

}
