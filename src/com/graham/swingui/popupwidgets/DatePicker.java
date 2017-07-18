/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.swingui.popupwidgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class DatePicker extends AbstractPopupWidget {
	//static final DateFormat dateTimeFormat =  DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
	
	static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
	
	private final Runnable cancel;
	
	private Calendar cal = Calendar.getInstance();
	private JTable yearTable;
	private JScrollPane yearTableScroll;
	private JTable monthTable;
	private JTable dayTable;
	private boolean ignoreListSel;
/*
	enum Mode {
		MonthDayYear,
		MonthDayYearTime,
		Duration,
		MonthAndDay
	}
	*/
	static class Event {
		int year;
		int month;
		int day;
		boolean everyYear;
		String title;
		Color color;
	}
	
	public DatePicker(final JFrame o, final JTextField tf, final boolean showTime) {
		super(o, tf);
		//owner = o;
		final String originalText = tf.getText();
	
		cancel =  () -> tf.setText(originalText);
		cal.setTime(tryToParseDate(tf.getText()));
	
		popupWindow.add(buildUI(new DayChangedListener(tf), new YearOrMonthChangedListener(tf)), BorderLayout.CENTER);
		popupWindow.pack();
		
		updateLocationRelativeToParent();

		popupWindow.setVisible(true);
	}
	
	//public void setEvents(List<Event> e) {
	//public void setDate(final Date d) {
	//public void show(final Date d, final int x, final int y) {
	//}
	
	private JPanel buildUI(ListSelectionListener dayChangedL, ListSelectionListener yearOrMonthL) {
		JPanel p = new JPanel(new BorderLayout());
		p.add(buildYears(), BorderLayout.WEST);
		p.add(buildMonths(), BorderLayout.CENTER);
		p.add(buildDays(), BorderLayout.EAST);
		
		setSelectedYearMonthDay();
		
		if (dayChangedL != null && yearOrMonthL != null) {
			yearTable.getSelectionModel().addListSelectionListener(yearOrMonthL);
			monthTable.getSelectionModel().addListSelectionListener(yearOrMonthL);
			dayTable.getSelectionModel().addListSelectionListener(dayChangedL); // only works for row change
			dayTable.getColumnModel().getSelectionModel().addListSelectionListener(dayChangedL); // only works for column change
		}
		
		return p;
	}
	
	private JScrollPane buildYears() {
		yearTable = new JTable(new YearTableModel());
		yearTable.setTableHeader(null);
	
		TableColumn column = yearTable.getColumnModel().getColumn(0);
		column.setPreferredWidth(70);

		yearTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		yearTableScroll = new JScrollPane(yearTable);
		yearTableScroll.setPreferredSize(new Dimension(70,100));
		
		return yearTableScroll;
	}

	private JTable buildMonths() {
		String[][] data = new String[12][1];
		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 2);
		c.set(Calendar.MONTH, Calendar.JANUARY);
		
		SimpleDateFormat f = new SimpleDateFormat("MMMMM");
		
		for (int i = 0; i < 12; i++) {
			data[i][0] = f.format(c.getTime());
			c.add(Calendar.MONTH, 1);
		}
	
		monthTable = new JTable(data, new String[]{"A"});
		monthTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		return monthTable;
	}
	
	private JPanel buildDays() {
		CalendarTableModel ctm = new CalendarTableModel();
		dayTable = new JTable(ctm);
		
		for (int i = 0; i < 7; i++) {
			final TableColumn column = dayTable.getColumnModel().getColumn(i);
			column.setPreferredWidth(34);
			column.setCellRenderer(new DayTableCellRenderer());
		}
		
		dayTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dayTable.setCellSelectionEnabled(true);
		dayTable.getTableHeader().setReorderingAllowed(false);
		dayTable.getTableHeader().setResizingAllowed(false);
		
		dayTable.getColumnModel().getColumn(0).setHeaderValue("Sun");
		dayTable.getColumnModel().getColumn(1).setHeaderValue("Mon");
		dayTable.getColumnModel().getColumn(2).setHeaderValue("Tue");
		dayTable.getColumnModel().getColumn(3).setHeaderValue("Wed");
		dayTable.getColumnModel().getColumn(4).setHeaderValue("Thu");
		dayTable.getColumnModel().getColumn(5).setHeaderValue("Fri");
		dayTable.getColumnModel().getColumn(6).setHeaderValue("Sat");
		
		JPanel p = new JPanel(new BorderLayout());
		p.add(dayTable.getTableHeader(), BorderLayout.NORTH);
		p.add(dayTable, BorderLayout.CENTER);
		return p;
	}
	
	private void setSelectedYearMonthDay() {
		YearTableModel ytm = (YearTableModel)yearTable.getModel();
		ytm.setStartYear(cal);
		ytm.fireTableDataChanged();
		
		CalendarTableModel ctm = (CalendarTableModel)dayTable.getModel();
		ctm.setYearAndMonth(cal);
		ctm.fireTableDataChanged();
		
		setSelectedYear(true);
		setSelectedMonth();
		setSelectedDay();
	}
	
	private void setSelectedYear(boolean scrollFlag) {
		int year = cal.get(Calendar.YEAR);
		YearTableModel tm = (YearTableModel)yearTable.getModel();
		int row = year - tm.startYear;
		yearTable.getSelectionModel().addSelectionInterval(row, row); // TODO if year is not in table, an exception will happen
		if (scrollFlag) {
			Rectangle r = yearTable.getCellRect(row - 3, 0, true);
			yearTableScroll.getViewport().scrollRectToVisible(r);
		} else {
			Rectangle r = yearTable.getCellRect(row + 8, 0, true);
			yearTableScroll.getViewport().setViewPosition(new Point(0,0));
			yearTableScroll.getViewport().scrollRectToVisible(r);
		}
	}
	
	private void setSelectedMonth() {
		int month = cal.get(Calendar.MONTH);
		monthTable.getSelectionModel().addSelectionInterval(month, month);
	}
	
	private void setSelectedDay() {
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		CalendarTableModel tm = (CalendarTableModel)dayTable.getModel();
		int[] rc = tm.getDateRowCol(day);
		
		dayTable.setColumnSelectionInterval(rc[1], rc[1]);
		dayTable.getSelectionModel().addSelectionInterval(rc[0], rc[0]);
	}
	
	private int getSelectedYear() {
		return (Integer)yearTable.getModel().getValueAt(yearTable.getSelectedRow(), 0);
	}
	
	private int getSelectedMonth() {
		return monthTable.getSelectedRow();
	}
	
	private int getSelectedDay() {
		Object v = dayTable.getModel().getValueAt(dayTable.getSelectedRow(), dayTable.getSelectedColumn());
		return (Integer)v;
	}
	
	private Date tryToParseDate(final String ds) {
		try {
			return dateFormat.parse(ds);
		} catch (Exception e) {
			return new Date();
		}
	}
	
	class DayChangedListener implements ListSelectionListener {
		final private JTextField tf;
		public DayChangedListener(final JTextField tfParam) {
			tf = tfParam;
		}
		
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return; 
			}
			if (ignoreListSel) {
				return; 
			}
			ignoreListSel = true;
			
			boolean setToNow = false;
			Date d = null;
			CalendarTableModel tm = (CalendarTableModel)dayTable.getModel();
			if (CalendarTableModel.isControlCell(dayTable.getSelectedRow(), dayTable.getSelectedColumn())) {
				if (dayTable.getSelectedColumn() == 4) { // Now
					d = new Date();
					setToNow = true;
				} else if (dayTable.getSelectedColumn() == 5) { // X
					cancel.run();
					popupWindow.setVisible(false);
					return;
				} else if (dayTable.getSelectedColumn() == 6) { // OK
					popupWindow.setVisible(false);
					return;
				}
			} else {
				System.out.println("D year=" + getSelectedYear() + "  month=" + getSelectedMonth() + "  day=" + getSelectedDay());
				d = tm.getDate(dayTable.getSelectedRow(), dayTable.getSelectedColumn());
			}
			Calendar c3 = Calendar.getInstance();
			c3.setTime(d);
			if (c3.get(Calendar.MONTH) != cal.get(Calendar.MONTH)) {
				cal.setTime(d);
				tm.setYearAndMonth(cal);
				tm.fireTableDataChanged();
				setSelectedYear(false);
				setSelectedMonth();
				setSelectedDay();
			} else {
				if (setToNow) {
					cal.setTime(d);
					setSelectedDay();
				} else {
					cal.set(Calendar.DAY_OF_MONTH, getSelectedDay());
				}
			}
			
			tf.setText(dateFormat.format(cal.getTime()));
			ignoreListSel = false;
		}
	}
	
	class YearOrMonthChangedListener implements ListSelectionListener {
		final private JTextField tf;
		public YearOrMonthChangedListener(final JTextField tfParam) {
			tf = tfParam;
		}
		
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return; 
			}
			if (ignoreListSel) {
				return; 
			}
			ignoreListSel = true;
			int day = getSelectedDay();
			System.out.println("YM year=" + getSelectedYear() + "  month=" + getSelectedMonth() + "  day=" + getSelectedDay());
			cal.set(getSelectedYear(), getSelectedMonth(), 1);
			if (day > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			} else {
				cal.set(Calendar.DAY_OF_MONTH, day);
			}
	
			CalendarTableModel ctm = (CalendarTableModel)dayTable.getModel();
			ctm.setYearAndMonth(cal);
			ctm.fireTableDataChanged();
			
			setSelectedDay();
			
			tf.setText(dateFormat.format(cal.getTime()));
			ignoreListSel = false;
		}
	}

	static class DayTableCellRenderer extends DefaultTableCellRenderer {
		final Calendar now;
		public DayTableCellRenderer() {
			now = Calendar.getInstance();
		}
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int col) {

			// Cells are by default rendered as a JLabel.
			JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

			// Get the status for the current row.
			if (!isSelected) {
				if (CalendarTableModel.isControlCell(row, col)) {
					if (col < 4) {
						l.setBackground(Color.WHITE);
					} else {
						//l.setFont(l.getFont().deriveFont(Font.BOLD));
						l.setBackground(Color.GRAY);
						//l.setHorizontalAlignment(JLabel.CENTER);
					}
				} else {
					CalendarTableModel tableModel = (CalendarTableModel) table.getModel();
					if (tableModel.isCurrentDay(row, col, now)) {
						l.setBackground(Color.DARK_GRAY);
					} else if (tableModel.inCurrentMonth(row, col)) {
						l.setBackground(Color.WHITE);
					} else {
						l.setBackground(Color.LIGHT_GRAY);
					}
				}
			}

			// Return the JLabel which renders the cell.
			return l;
		}
	}
}
