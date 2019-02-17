/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import com.graham.passvaultplus.model.core.PvpRecord;

public interface BCTableModel {

	int getColumnCount();
	
	int getRowCount();

	Object getValueAt(int rowIndex, int columnIndex, boolean returnSecretRealValue);

	Object getValueAt(int rowIndex, int columnIndex);
	
	PvpRecord getRecordAtRow(final int rowIndex);
	
	void flushCache();
	
}
