/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.List;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.view.CategoryMenuItem;

public class RecordFilter {

	private PvpContext context;
	private List<PvpRecord> data;
	private boolean allTheSameTypeFlag;
	private BCTableModel allTheSameTypeModel;
	private BCTableModel differentTypesModel;

	public RecordFilter(final PvpContext c) {
		context = c;
		allTheSameTypeModel = new BCTableModelHomog2(this, context);
		differentTypesModel = new BCTableModelHeter(this, context);
	}

	/**
	 * Called when one of the filter UI items changes.
	 */
	public void filterUIChanged() {
		data = null;
		allTheSameTypeModel.flushCache();
		differentTypesModel.flushCache();
	}

	public BCTableModel getCurrentModel() {
		if (isAllTheSameType()) {
			return allTheSameTypeModel;
		} else {
			return differentTypesModel;
		}
	}

	private void doWork() {
		String filterByType = context.getViewListContext().getTypeComboBox().getSelectedItem().toString();
		String filterByText = context.getViewListContext().getFilterTextField().getText();
		CategoryMenuItem filterByCategory = (CategoryMenuItem)context.getViewListContext().getCategoryComboBox().getSelectedItem();
		boolean checkCategory = !PvpRecord.FILTER_ALL_CATEGORIES.equals(filterByCategory.toString());

		PvpDataInterface.FilterResults results = context.getDataInterface().getFilteredRecords(filterByType, filterByText, filterByCategory.getCategory(), checkCategory);
		data = results.records;
		allTheSameTypeFlag = results.allTheSameTypeFlag;
		context.getViewListContext().getRecordCountLabel().setText(data.size() + " record" + BCUtil.getPluralAppendix(data.size()));
	}

	public int getRecordCount() {
		if (data == null) {
			doWork();
		}
		return data.size();
	}

	public PvpRecord getRecordAtIndex(final int index) {
		if (data == null) {
			doWork();
		}
		return (PvpRecord)data.get(index);
	}

	public boolean isAllTheSameType() {
		if (data == null) {
			doWork();
		}
		return allTheSameTypeFlag;
	}

}
