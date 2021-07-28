/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.dashboard;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.table.AbstractTableModel;

import com.graham.util.DateUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpRecord;

public class UpcomingDatesTableModel extends AbstractTableModel {

	static class UpcomingDate {
		final PvpRecord record;
		final Date d;
		final String dateString;
		final int daysOut;
		
		UpcomingDate(final PvpRecord r, final Date dParam, final String ds) {
			record = r;
			d = dParam;
			dateString = ds;
			if (d != null) {
				daysOut = (int)TimeUnit.DAYS.convert(2*60*60*1000 + d.getTime() - new Date().getTime(), TimeUnit.MILLISECONDS);
			} else {
				daysOut = 0;
			}
		}
	}
	
	static class UpcomingDateComparator implements Comparator<UpcomingDate> {
		@Override
		public int compare(UpcomingDate o1, UpcomingDate o2) {
			if (o1.d == null) {
				if (o2.d == null) {
					return 0;
				} else {
					return -1;
				}
			}
			if (o2.d == null) {
				return 1;
			}
			return o1.d.compareTo(o2.d);
		}
	}
	
	private List<UpcomingDate> records = new ArrayList<>();
	
	public UpcomingDatesTableModel(PvpContext context) {
		loadDays(context, 14);
	}
	
	public void setNumberOfDays(PvpContext context, int days) {
		records = new ArrayList<>();
		loadDays(context, days);
		fireTableDataChanged();
	}
	
	private void loadDays(PvpContext context, int days) {

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, days);
		Date cutoffDate = cal.getTime();
		
		List<PvpRecord> allRecords = context.data.getDataInterface().getRecords();
		
		for (PvpRecord r : allRecords) {
			if (r.isArchived()) {
				continue;
			}
			String t = r.getType().getName();
			if (t.equals("Todo")) { // TODO : a way to link this if user changes type name ?
				Date d = null;
				String completeDate = r.getCustomField("Complete Date");
				try {
					d = DateUtil.parseDateLocalized(completeDate);
				} catch (ParseException e) {
					//e.printStackTrace();
				}
				if (d != null) {
					if (d.before(cutoffDate)) {
						records.add(new UpcomingDate(r, d, completeDate));
					}
				} else {
					records.add(new UpcomingDate(r, null, completeDate));
				}
			} else if (t.equals("Important Date")) {
				String dateString = r.getCustomField("Date");
				Date d = DateUtil.parseUpcomingDate(dateString);
				if (d != null) {
					if (d.before(cutoffDate)) {
						records.add(new UpcomingDate(r, d, dateString));
					}
				} else {
					records.add(new UpcomingDate(r, null, dateString));
				}
			}
		}
		
		Collections.sort(records, new UpcomingDateComparator());
	}
	
	@Override
	public int getRowCount() {
		return records.size();
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		UpcomingDate ud = records.get(rowIndex);
		if (columnIndex == 0) {
			return ud.daysOut;
		} else if (columnIndex == 1) {
			return ud.dateString;
		} else if (columnIndex == 2) {
			return ud.record.getType().getName();
		} else {
			return ud.record.toString();
		}
	}

	public PvpRecord getRecordAtRow(int rowIndex) {
		return records.get(rowIndex).record;
	}
	
}
