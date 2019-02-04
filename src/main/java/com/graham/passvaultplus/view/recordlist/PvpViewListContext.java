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
		setTableHeaders();
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
		setTableHeaders();
	}
	
	private void setTableHeaders() {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0107");
		listTable.getColumnModel().getColumn(0).setHeaderValue("Field");
		listTable.getColumnModel().getColumn(1).setHeaderValue("Value");
	}
	
}
