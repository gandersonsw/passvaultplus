/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.swingui.datepicker;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.swing.table.AbstractTableModel;

public class CalendarTableModel extends AbstractTableModel {
	
	static public final int CONTROL_ROW = 10;
	
	private int year;
	private int month;
	private Calendar firstDay = Calendar.getInstance();
	
	public void setYearAndMonth(final Calendar cal) {
		year = cal.get(Calendar.YEAR);
		month = cal.get(Calendar.MONTH);
		firstDay.set(year, month, 1, 0, 0, 0);
		int weekday = firstDay.get(Calendar.DAY_OF_WEEK);
		firstDay.add(Calendar.DAY_OF_YEAR, -13 - weekday);
	}
	
	@Override
	public int getRowCount() {
		return 11;
	}

	@Override
	public int getColumnCount() {
		return 7;
	}

	public static boolean isControlCell(int rowIndex, int columnIndex) {
		return rowIndex == CONTROL_ROW && columnIndex > 3;
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (isControlCell(rowIndex, columnIndex)) {
			if (columnIndex == 6) {
				return " OK";
			} if (columnIndex == 5) {
				return "  X";
			} if (columnIndex == 4) {
				return "Now";
			} else {
				return "";
			}
		} else {
			int daysToAdd = rowIndex * 7 + columnIndex;
			Calendar c2 = Calendar.getInstance();
			c2.setTime(firstDay.getTime());
			c2.add(Calendar.DAY_OF_YEAR, daysToAdd);
			return c2.get(Calendar.DAY_OF_MONTH);
		}
	}
	
	public boolean inCurrentMonth(int rowIndex, int columnIndex) {
		int daysToAdd = rowIndex * 7 + columnIndex;
		Calendar c2 = Calendar.getInstance();
		c2.setTime(firstDay.getTime());
		c2.add(Calendar.DAY_OF_YEAR, daysToAdd);
		return c2.get(Calendar.MONTH) == month;
	}
	
	public Date getDate(int rowIndex, int columnIndex) {
		int daysToAdd = rowIndex * 7 + columnIndex;
		Calendar c2 = Calendar.getInstance();
		c2.setTime(firstDay.getTime());
		c2.add(Calendar.DAY_OF_YEAR, daysToAdd);
		return c2.getTime();
	}

	public int[] getDateRowCol(final int dayOfMonth) {
		Calendar c2 = Calendar.getInstance();
		c2.set(year, month, dayOfMonth, 0, 0, 0);
		int days = (int)TimeUnit.DAYS.convert(2*60*60*1000 + c2.getTimeInMillis() - firstDay.getTimeInMillis(), TimeUnit.MILLISECONDS);
		int row = days / 7;
		int col = days % 7;
		return new int[]{row,col};
	}

}
