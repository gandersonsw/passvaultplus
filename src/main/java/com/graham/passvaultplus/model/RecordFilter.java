/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.Collections;
import java.util.List;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.AppUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.model.core.PvpRecordComparator;
import com.graham.passvaultplus.model.core.PvpType;
import com.graham.passvaultplus.view.CategoryMenuItem;

public class RecordFilter {

	private PvpContext context;
	private List<PvpRecord> data;
	private boolean allTheSameTypeFlag;

	private BCTableModelHeterVert modelHeterVert;
	private BCTableModelHeterHorz modelHeterHorz;
	private BCTableModelHomogVert modelHomogVert;
	private BCTableModelHomogHorz modelHomogHorz;

	private BCTableModel currentModel;

	public RecordFilter(final PvpContext c) {
		context = c;
	}

	/**
	 * Called when one of the filter UI items changes.
	 */
	public void filterUIChanged() {
		currentModel = null;
		data = null;
		if (modelHeterVert != null) {
			modelHeterVert.flushCache();
		}
		if (modelHeterHorz != null) {
			modelHeterHorz.flushCache();
		}
		if (modelHomogVert != null) {
			modelHomogVert.flushCache();
		}
		if (modelHomogHorz != null) {
			modelHomogHorz.flushCache();
		}
	}

	public BCTableModel getCurrentModel() {
		if (currentModel != null) {
			return currentModel;
		}
		boolean isVert = true;
		switch (context.prefs.getRecordListViewOptions().getVertTableLayout()) {
			case RecordListViewOptions.VTL_NEVER:
				isVert = false;
				break;
			case RecordListViewOptions.VTL_ONLY_1:
				isVert = getRecordCount() == 1;
				break;
			case RecordListViewOptions.VTL_LT_5:
				isVert = getRecordCount() < 5;
				break;
			case RecordListViewOptions.VTL_GT_1_TYPE:
				isVert = !isAllTheSameType();
				break;
			case RecordListViewOptions.VTL_ALWAYS:
				isVert = true;
				break;
			default:
				context.ui.notifyWarning("RecordFilter.getCurrentModel: bad VertTableLayout value: " + context.prefs.getRecordListViewOptions().getVertTableLayout());
				isVert = true;
				break;
		}

		if (isAllTheSameType()) {
			if (isVert) {
				if (modelHomogVert == null) {
					modelHomogVert = new BCTableModelHomogVert(this, context);
				}
				currentModel = modelHomogVert;
			} else {
				if (modelHomogHorz == null) {
					modelHomogHorz = new BCTableModelHomogHorz(this, context);
				}
				currentModel = modelHomogHorz;
			}
		} else {
			if (isVert) {
				if (modelHeterVert == null) {
					modelHeterVert = new BCTableModelHeterVert(this, context);
				}
				currentModel = modelHeterVert;
			} else {
				if (modelHeterHorz == null) {
					modelHeterHorz = new BCTableModelHeterHorz(this, context);
				}
				currentModel = modelHeterHorz;
			}
		}

		return currentModel;
	}

	private void doWork() {
		String filterByType = context.uiMain.getViewListContext().getTypeComboBox().getSelectedItem().toString();
		String filterByText = context.uiMain.getViewListContext().getFilterTextField().getText();
		CategoryMenuItem filterByCategory = (CategoryMenuItem)context.uiMain.getViewListContext().getCategoryComboBox().getSelectedItem();
		boolean checkCategory = !PvpRecord.FILTER_ALL_CATEGORIES.equals(filterByCategory.toString());

		PvpDataInterface.FilterResults results = context.data.getDataInterface().getFilteredRecords(filterByType, filterByText, filterByCategory.getCategory(), checkCategory);
		data = results.records;
		allTheSameTypeFlag = results.allTheSameTypeFlag;
		context.uiMain.getViewListContext().getRecordCountLabel().setText(data.size() + " record" + BCUtil.getPluralAppendix(data.size()));
		doSort();
	}

	private void doSort() {
		PvpField sort = context.prefs.getRecordListViewOptions().getSort();
		if (sort == null) {
			return;
		}
		Collections.sort(data, new PvpRecordComparator(sort, context.prefs.getRecordListViewOptions().getSortDir() == RecordListViewOptions.SORT_ASC));
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

	public PvpType getTypeIfAllSame() {
		if (data == null) {
			doWork();
		}
		if (allTheSameTypeFlag && data.size() > 0) {
			return data.get(0).getType();
		}
		return null;
	}

}
