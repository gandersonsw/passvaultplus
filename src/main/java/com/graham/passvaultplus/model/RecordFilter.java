/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.model.core.PvpRecordComparator;
import com.graham.passvaultplus.model.core.PvpType;
import com.graham.passvaultplus.view.CategoryMenuItem;
import com.graham.util.StringUtil;

import com.graham.passvaultplus.model.search.SearchResults;
import com.graham.passvaultplus.model.search.SearchRecord;
import com.graham.passvaultplus.model.search.DefaultSearchRecordComparator;
import com.graham.passvaultplus.model.search.SearchRecordComparator;

public class RecordFilter {

	private PvpContext context;
	private SearchResults data;
	private BCTableModelHeterVert modelHeterVert;
	private BCTableModelHeterHorz modelHeterHorz;
	private BCTableModelHomogVert modelHomogVert;
	private BCTableModelHomogHorz modelHomogHorz;
	private BCTableModelHeterVertDetailed modelHeterVertDetailed;
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
		if (modelHeterVertDetailed != null) {
			modelHeterVertDetailed.flushCache();
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
				if (getRecordCount() < 26) {
					// show more details if there are a limited number of records
					if (modelHeterVertDetailed == null) {
						modelHeterVertDetailed = new BCTableModelHeterVertDetailed(this, context);
					}
					currentModel = modelHeterVertDetailed;
				} else {
					if (modelHeterVert == null) {
						modelHeterVert = new BCTableModelHeterVert(this, context);
					}
					currentModel = modelHeterVert;
				}
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
		data = context.data.getDataInterface().getFilteredRecords(filterByType, filterByText, filterByCategory.getCategory(), checkCategory);
		context.uiMain.getViewListContext().getRecordCountLabel().setText(data.records.size() + " record" + StringUtil.getPluralAppendix(data.records.size()));
		doSort();
	}

	private void doSort() {
		PvpField sort = context.prefs.getRecordListViewOptions().getSort();
		if (sort == null || sort.getCoreFieldId() == PvpField.CFID_SEARCH_MATCH) {
			DefaultSearchRecordComparator.doSort(data, context.prefs.getRecordListViewOptions().getSortDir() == RecordListViewOptions.SORT_ASC);
		} else {
			Collections.sort(data.records, new SearchRecordComparator(sort, context.prefs.getRecordListViewOptions().getSortDir() == RecordListViewOptions.SORT_ASC));
		}
	}

	public int getRecordCount() {
		if (data == null) {
			doWork();
		}
		return data.records.size();
	}

	public SearchRecord getRecordAtIndex(final int index) {
		if (data == null) {
			doWork();
		}
		return data.records.get(index);
	}

	public boolean isAllTheSameType() {
		if (data == null) {
			doWork();
		}
		return data.allTheSameTypeFlag;
	}
	
	public boolean isAllTheSameMatch() {
		if (data == null) {
			doWork();
		}
		return data.allTheSameMatchFlag;
	}

	public PvpType getTypeIfAllSame() {
		if (data == null) {
			doWork();
		}
		if (data.allTheSameTypeFlag && data.records.size() > 0) {
			return data.records.get(0).record.getType();
		}
		return null;
	}

}
