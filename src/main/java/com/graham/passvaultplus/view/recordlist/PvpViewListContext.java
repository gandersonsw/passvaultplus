/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordlist;

import java.util.*;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.graham.passvaultplus.model.ListTableModel;
import com.graham.passvaultplus.model.core.PvpRecord;

/**
 * This holds data that was initialized in the creation of the list tab. MainFrame, calls ViewListBuilder.
 */
public class PvpViewListContext {
	
	private JComboBox typeComboBox;
	private JComboBox categoryComboBox;

	private JTable listTable;
	private ListTableModel listTableModel;
	private JTextField filterTextField;
	private JLabel recordCount;

	void setTypeComboBox(final JComboBox c) {
		typeComboBox = c;
	}
	
	public JComboBox getTypeComboBox() {
		return typeComboBox;
	}
	
	void setCategoryComboBox(final JComboBox c) {
		categoryComboBox = c;
	}
	
	public JComboBox getCategoryComboBox() {
		return categoryComboBox;
	}
	
	/**
	 * This can return null.
	 */
	public PvpRecord getFirstSelectedRecord() {
		int row = listTable.getSelectedRow();
		if (row < 0) {
			return null;
		}
		return listTableModel.getRecordAtRow(row);
	}
	
	/**
	 * List of RtRecords. Will not return null. May return an empty collection.
	 */
	public Collection<PvpRecord> getAllSelectedRecords() {
		int rows[] = listTable.getSelectedRows();
		HashMap<Integer, PvpRecord> records = new HashMap<>();

		for (int i = 0; i < rows.length; i++) {
			PvpRecord r = listTableModel.getRecordAtRow(rows[i]);
			if (r != null) {
				records.put(r.getId(), r);
			}
		}
		return records.values();
	}
	
	void setListTable(final JTable t, final ListTableModel tm) {
		listTable = t;
		listTableModel = tm;
	}
	
	void setFilterTextField(final JTextField tf) {
		filterTextField = tf;
	}
	
	public JTextField getFilterTextField() {
		return filterTextField;
	}

	void setRecordCountLabel(final JLabel rc) {
		recordCount = rc;
	}
	
	public JLabel getRecordCountLabel() {
		return recordCount;
	}
	
	public void filterUIChanged() {
		listTableModel.filterUIChanged();
		setColPrefWidths();
	}
	
	public void setColPrefWidths() {
		int tablePreferredWidth = listTable.getWidth();

		if (listTableModel.isVertModel()) {
			if (listTableModel.getColumnCount() == 2) {
				listTable.getColumnModel().getColumn(0).setPreferredWidth(150);
				listTable.getColumnModel().getColumn(1).setPreferredWidth(tablePreferredWidth - 150);
			} else {
				listTable.getColumnModel().getColumn(0).setPreferredWidth(20);
				listTable.getColumnModel().getColumn(1).setPreferredWidth(150);
				listTable.getColumnModel().getColumn(2).setPreferredWidth(tablePreferredWidth - 170);
			}
		} else if (!listTableModel.isAllTheSameMatch()) {
			int startI = 0;
			int w = tablePreferredWidth / listTableModel.getColumnCount();
			if (!listTableModel.isAllTheSameMatch()) { // dup check here because will eventually do some work either way
				listTable.getColumnModel().getColumn(0).setPreferredWidth(20);
				w = (tablePreferredWidth - 20) / (listTableModel.getColumnCount() - 1);
				startI = 1;
			}
			for (int i = startI; i < listTableModel.getColumnCount(); i++) {
				listTable.getColumnModel().getColumn(i).setPreferredWidth(w);
			}
		}
	}
	
}
