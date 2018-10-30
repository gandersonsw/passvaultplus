/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.graham.passvaultplus.PvpContext;

/**
 * This is basically a database. It represents a set of types and data records that bundle together.
 * All methods to work with RtRecords and RtTypes.
 */
public class PvpDataInterface {

	static final public String TYPE_CATEGORY = "Category";
	static final public String TYPE_PASSWORD_GEN = "Password Generator";

	static public class FilterResults {
		public List<PvpRecord> records = new ArrayList<PvpRecord>();
		public boolean allTheSameTypeFlag = true;
	}

	private final PvpContext context;
	private List<PvpType> types;
	private List<PvpRecord> records;
	private int maxID;

	public PvpDataInterface(final PvpContext contextParam) {
		context = contextParam;
	}

	public PvpDataInterface(final PvpContext contextParam, List<PvpType> typesParam, List<PvpRecord> recordsParam, int maxIDParam) {
		context = contextParam;
		types = typesParam;
		records = recordsParam;
		maxID = maxIDParam;
	}

	void setData(PvpDataInterface dataTocCopyFrom) {
		types = dataTocCopyFrom.types;
		records = dataTocCopyFrom.records;
		maxID = dataTocCopyFrom.maxID;
	}

	/**
	 * return true if the original data has been appended or updated, so it should be written out to the file
	 */
	PvpDataMerger.MergeResultState mergeData(PvpDataInterface dataToMergeFrom) {
		PvpDataMerger m = new PvpDataMerger(context);
		return m.mergeData(this, dataToMergeFrom);
	}

	/**
	 * @return List of PvpType
	 */
	public List<PvpType> getTypes() {
		return types;
	}

	public List<PvpRecord> getCategories() {
		return getRecordsOfType(TYPE_CATEGORY);
	}

	public List<PvpRecord> getRecordsOfType(String typeName) {
		// TODO optimize this
		List<PvpRecord> ret = new ArrayList<PvpRecord>();
		for (PvpRecord r : records) {
			if (PvpType.sameType(r.getType(), typeName)) {
				ret.add(r);
			}
		}
		return ret;
	}

	public PvpType getType(final String typeName) {
		for (PvpType t : types) {
			if (PvpType.sameType(t, typeName)) {
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
			int nextID = getNextMaxId();
			r.setId(nextID);
			records.add(r);
		}
		context.getFileInterface().save(this, PvpPersistenceInterface.SaveTrigger.cud); // TODO remove dependance
		context.getViewListContext().filterUIChanged();
	}

	public void deleteRecord(final PvpRecord r) {
		if (records.contains(r)) {
			records.remove(r);
			r.setId(0);
			context.getFileInterface().save(this, PvpPersistenceInterface.SaveTrigger.cud); // TODO remove dependence
			context.getViewListContext().filterUIChanged();
		} else {
			context.notifyWarning("WARN111 could not delete record because it is not in the list:" + r);
		}
	}

	public void saveRecords(final Collection<PvpRecord> rCol) {
		if (rCol == null || rCol.size() == 0) {
			return;
		}
		for (PvpRecord r : rCol) {
			if (r.getId() == 0) {
				int nextID = getNextMaxId();
				r.setId(nextID);
				records.add(r);
			}
		}
		context.getFileInterface().save(this, PvpPersistenceInterface.SaveTrigger.cud); // TODO remove dependance on getFileInterface
		context.getViewListContext().filterUIChanged();
	}

	public void deleteRecords(final Collection<PvpRecord> rCol) {
		if (rCol == null || rCol.size() == 0) {
			return;
		}
		boolean changed = false;
		for (PvpRecord r : rCol) {
			if (records.contains(r)) {
				records.remove(r);
				r.setId(0);
				changed = true;
			} else {
				context.notifyWarning("WARN112 could not delete record because it is not in the list:" + r);
			}
		}

		if (changed) {
			context.getFileInterface().save(this, PvpPersistenceInterface.SaveTrigger.cud); // TODO remove dependence
			context.getViewListContext().filterUIChanged();
		}
	}

	public int getRecordCount() {
		return records.size();
	}

	public PvpRecord getRecordAtIndex(final int index) {
		return records.get(index);
	}

	public PvpRecord getRecord(final int recordID) {
		// TODO optimize this
		for (PvpRecord r : records) {
			if (recordID == r.getId()) {
				return r;
			}
		}
		return null;
	}

	public List<PvpRecord> getRecordsByToString(final PvpType t, final String s) {
		// TODO optimize this
		List<PvpRecord> results = new ArrayList<>();
		if (s == null || s.length() == 0) {
			return results;
		}
		for (PvpRecord r : records) {
			if (PvpType.sameType(r.getType(), t) && s.equals(r.toString())) {
				results.add(r);
			}
		}
		return results;
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
				} else if (counts.containsKey(val)) {
					counts.put(val, counts.get(val) + 1);
				} else {
					counts.put(val, 1);
				}
			}
		}

		// TODO sort by counts ? or by aphabetical ?

		List<String> values = new ArrayList<>();
		for (Map.Entry<String, Integer> e : counts.entrySet()) {
			if (e.getValue() > 1) {
				values.add(e.getKey());
			}
		}

		return values;
	}

	public FilterResults getFilteredRecords(final String filterByType, final String filterByText, final PvpRecord filterByCategory, final boolean checkCategory) {
		final long startTime = System.nanoTime();

		boolean checkType = !filterByType.equals(PvpType.FILTER_ALL_TYPES);
		boolean checkText = filterByText.length() > 0;
		List<PvpRecord> allRecords = context.getDataInterface().getRecords(); // TODO delete context.getDataInterface().

    	final String filterByTextLC = filterByText.toLowerCase();

    	FilterResults results = new FilterResults();

    	Stream<PvpRecord> filteredStream = allRecords.stream().filter(r -> {
			if (checkType) {
				if (!PvpType.sameType(r.getType(), filterByType)) {
					return false;
				}
			}
			if (checkCategory) {
				if (filterByCategory == null) {
					if (r.getCategory() != null) {
						return false;
					}
				} else if (r.getCategory() == null) {
					return false;
				} else if (!(r.getCategory().getId() == filterByCategory.getId())) {
					return false;
				}
			}
			if (checkText) {
				if (!recordContainsText(r, filterByTextLC)) {
					return false;
				}
			}
			return true;
   	 	});

    	results.records = filteredStream.collect(Collectors.toList());

    	if (checkType) {
    		results.allTheSameTypeFlag = true;
    	} else {
    		results.allTheSameTypeFlag = results.records.stream().map(r -> r.getType().getName()).distinct().count() == 1;
    	}

    	if (context.getShowDiagnostics()) {
    		final long endTime = System.nanoTime();
				final long msDelta = (endTime - startTime) / 1000000;
				if (msDelta > 1) {
    			context.notifyInfo("PvpDataInterface.getFilteredRecords :: time: " + msDelta + "ms : " + results.records.size() + " : " + filterByType + " : " + filterByText + " : " + filterByCategory);
				}
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

	public int getNextMaxId() {
		maxID++;
		return maxID;
	}

	public int getMaxId() {
		return maxID;
	}

}
