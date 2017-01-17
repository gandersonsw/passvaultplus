/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.graham.passvaultplus.PvpContext;

/**
 * All methods to work with RtRecords and RtTypes.
 */
public class PvpDataInterface {
	
	static final public String TYPE_CATEGORY = "Category";

	static public class FilterResults {
		public List<PvpRecord> records = new ArrayList<PvpRecord>();
		public boolean allTheSameTypeFlag = true;
	}

	List<PvpType> types;
	List<PvpRecord> records;
	PvpContext context;

	public PvpDataInterface(final PvpContext contextParam) {
		context = contextParam;
	}

	public PvpDataInterface(final PvpContext contextParam, List<PvpType> typesParam, List<PvpRecord> recordsParam) {
		context = contextParam;
		types = typesParam;
		records = recordsParam;
	}

  	void setData(PvpDataInterface dataTocCopyFrom) {
		types = dataTocCopyFrom.types;
		records = dataTocCopyFrom.records;
	}

	/**
	 * @return List of RtType
	 */
	public List<PvpType> getTypes() {
		return types;
	}

	public List<PvpRecord> getCategories() {
		// TODO optimize this
		List<PvpRecord> ret = new ArrayList<PvpRecord>();
		for (PvpRecord r : records) {
			if (TYPE_CATEGORY.equals(r.getType().getName())) {
				ret.add(r);
			}
		}
		return ret;
	}

	public PvpType getType(final String typeName) {
		for (PvpType t : types) {
			if (t.getName().equals(typeName)) {
				return t;
			}
		}
		return null;
	}

	/**
	 * @return List of RtRecord
	 */
	public List<PvpRecord> getRecords() {
		return records;
	}

	public void saveRecord(final PvpRecord r) {
		if (r.getId() == 0) {
			int maxID = context.getFileInterface().getNextMaxID();
			r.setId(maxID);
			records.add(r);
		}
		context.getFileInterface().save(this);
		context.getViewListContext().getListTableModel().filterUIChanged();
	}

	public void deleteRecord(final PvpRecord r) {
		if (records.contains(r)) {
			records.remove(r);
			context.getFileInterface().save(this);
			context.getViewListContext().getListTableModel().filterUIChanged();
		} else {
			context.notifyWarning("could not delete record because it is not in the list:" + r);
		}
	}

	public int getRecordCount() {
		return records.size();
	}

	public PvpRecord getRecordAtIndex(final int index) {
		return records.get(index);
	}

	/*
	public void mergeFile(File f) {

	}
	*/

	/*
	public void saveXsd(File xsdFile) {

	}
	*/

//	private String getXmlFilePath() {
//		return context.getDataFilePath();
//	}

	public PvpRecord getRecord(final int recordID) {
		// TODO optimize this
		for (PvpRecord r : records) {
			if (recordID == r.getId()) {
				return r;
			}
		}
		return null;
	}


  public List<String> getCommonFiledValues(String recordType, String recordFieldName) {
		FilterResults filtered = getFilteredRecords(recordType, "", null, false);

    HashMap<String, Integer> counts = new HashMap<>();

		for (PvpRecord r : filtered.records) {
			String val = r.getCustomField(recordFieldName);
			if (val != null) {
				val = val.trim();
				if (val.length() == 0) {
					// ignore empty strings
				}else if (counts.containsKey(val)) {
					counts.put(val, counts.get(val) + 1);
				} else {
					counts.put(val, 1);
				}
			}
		}

		// TODO sort by counts ?  or by aphabetical ?

		List<String> values = new ArrayList<>();
		for (Map.Entry<String, Integer> e : counts.entrySet()) {
			if (e.getValue() > 1) {
			  values.add(e.getKey());
		  }
		}

		return values;
	}

	public FilterResults getFilteredRecords(String filterByType, String filterByText, PvpRecord filterByCategory, boolean checkCategory) {
		boolean checkType = !filterByType.equals(PvpType.FILTER_ALL_TYPES);
		boolean checkText = filterByText.length() > 0;
		List<PvpRecord> allRecords = context.getDataInterface().getRecords();

    	filterByText = filterByText.toLowerCase();

   	 	FilterResults results = new FilterResults();

		for (int i = 0; i < allRecords.size(); i++) {
			PvpRecord r = allRecords.get(i);
			if (checkType) {
				if (!r.getType().getName().equals(filterByType)) {
					continue;
				}
			}
			if (checkCategory) {
				if (filterByCategory == null) {
					if (r.getCategory() != null) {
						continue;
					}
				} else if (r.getCategory() == null) {
					continue;
				} else if (!(r.getCategory().getId() == filterByCategory.getId())) {
					continue;
				}
			}
			if (checkText) {
				if (!recordContainsText(r, filterByText)) {
					continue;
				}
			}

			if (results.records.size() > 0) {
				String type = results.records.get(0).getType().getName();
				if (!type.equals(r.getType().getName())) {
					results.allTheSameTypeFlag = false;
				}
			}

			results.records.add(r);
		}

		return results;
	}

	private boolean recordContainsText(final PvpRecord r, final String filterByText) {
		for (final String s : r.getCustomFields().values()) {
			if (s != null) {
				if (s.toLowerCase().indexOf(filterByText) != -1) {
					return true;
				}
			}
		}
		return false;
	}

}
