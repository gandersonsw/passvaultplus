/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.swingui.popupwidgets;

import java.util.Calendar;

import javax.swing.table.AbstractTableModel;

public class YearTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	
	int startYear;
	
	public void setStartYear(final Calendar cal) {
		startYear = cal.get(Calendar.YEAR) - 100;
	}
	
	@Override
	public int getRowCount() {
		return 200;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return startYear + rowIndex;
	}

}
