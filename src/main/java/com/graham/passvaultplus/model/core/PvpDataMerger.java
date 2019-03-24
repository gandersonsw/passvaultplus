/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.graham.passvaultplus.PvpContext;

/**
 * Merge 2 datasets. The TO dataset is considered the master.
 * NO_CHANGE    -> means no serialization of datasets needed
 * FROM_CHANGED -> means the FROM dataset should be overwritten with the new TO dataset
 * TO_CHANGED   -> means the TO dataset should be written
 * BOTH_CHANGED -> means both datasets should be serialized using the TO dataset.
 *
 * Both datasets should be identical, so the TO and FROM should be the same at the end (after serialization)
 */
public class PvpDataMerger {

	public static class DelRec {
		final public boolean inFromDb;
		final public PvpRecord rec;
		public DelRec(PvpRecord r, boolean inFromDbParam) {
			rec = r;
			inFromDb = inFromDbParam;
		}
	}

	public enum MergeResultState {
		NO_CHANGE(0),    // the 2 datasets are syncronized already
		FROM_CHANGED(1), // the FROM dataset was changed with newer updates from the TO dataset
		TO_CHANGED(2),   // the TO dataset was changed with newer updates from the FROM dataset
		BOTH_CHANGED(3); // both datasets needed updates from each other

		final int bits;

		MergeResultState(int bitsParam) {
			bits = bitsParam;
		}

		static MergeResultState getByBits(int bitsParam) {
			switch (bitsParam) {
			case 0:
				return NO_CHANGE;
			case 1:
				return FROM_CHANGED;
			case 2:
				return TO_CHANGED;
			case 3:
				return BOTH_CHANGED;
			}
			throw new RuntimeException("invalid bitsParam");
		}
	}

	private static final Date ZERO_DATE = new Date(0);
	static boolean USE_DELETE_UI = true; // this is temporary until the delete is working perfectly

	private final PvpContext context;
	private final PvpDataInterface dataToMergeTo;
	private final PvpDataInterface dataToMergeFrom;

	private MergeResultState resultState;
	private PvpRecord fromRec;
	private int curLogInofId;
	private int i;
	private int sameModDateCount;
	private int indexNotMatching;
	private int recordsMatched;
	private boolean[] matchedRecords;
	private int maxIdMatching;

	public PvpDataMerger(final PvpContext contextParam, PvpDataInterface dataToMergeToParam, PvpDataInterface dataToMergeFromParam) {
		context = contextParam;
		dataToMergeTo = dataToMergeToParam;
		dataToMergeFrom = dataToMergeFromParam;
	}

	/**
	 * if param is true, add the "From" PvpDataInterface to the ones that are
	 * dirty (that have been modified)
	 */
	private void dirtyFrom(boolean b) {
		if (b) {
			resultState = MergeResultState.getByBits(resultState.bits | MergeResultState.FROM_CHANGED.bits);
		}
	}

	/**
	 * if param is true, add the "To PvpDataInterface to the ones that are dirty
	 * (that have been modified)
	 */
	private void dirtyTo(boolean b) {
		if (b) {
			resultState = MergeResultState.getByBits(resultState.bits | MergeResultState.TO_CHANGED.bits);
		}
	}

	private void logRecInfo(String s) {
		if (fromRec.getId() != curLogInofId) {
			curLogInofId = fromRec.getId();
			context.ui.notifyInfo("> Record Index:" + i + " Id:" + fromRec.getId() + ":" + fromRec + " ----------");
		}
		context.ui.notifyInfo(s);
	}

	/**
	 * dataToMergeTo is considered the newer dataset. dataToMergeFrom is considered the older dataset.
	 */
	public MergeResultState mergeData() {

		resultState = MergeResultState.NO_CHANGE;

		context.ui.notifyInfo(">>>>>>>>>> start merge. toMaxId:" + dataToMergeTo.getMaxId() + " fromMaxId:" + dataToMergeFrom.getMaxId());

		int thisStartingRecordCount = dataToMergeTo.getRecordCount();
		maxIdMatching = 0; // the largest ID that existed in both databases that match
		List<DelRec> delRecs = new ArrayList<>();

		// IDs of records in the main database that have a corresponding record in the mergeFrom database
		matchedRecords = new boolean[dataToMergeTo.getMaxId() + 1];
		int typesMatched = 0;
		int toTypeCount = dataToMergeTo.getTypes().size();

		for (PvpType fromType : dataToMergeFrom.getTypes()) {
			PvpType toType = dataToMergeTo.getType(fromType.getName());
			if (toType == null) {
				context.ui.notifyInfo("> adding type (FROM):" + fromType.getName());
				dataToMergeTo.getTypes().add(fromType);
				dirtyTo(true);
			} else {
				// TODO compare and modify
				// If the user has modified a type in the older dataset, it will be overwritten by the type from the newer dataset. 
				// Since we don't track the modification date for types, there is not a way to handle this for now.
				typesMatched++;
			}
		}
		// Since we do not track modified date for types, assume this was a type delete
		//		if (typesMatched < toTypeCount) {
		//			context.ui.notifyInfo("> typesMatched < dataToMergeTo.getTypes().size()");
		//			dirtyFrom(true);
		//		}

		indexNotMatching = 0;
		recordsMatched = 0;

		for (i = dataToMergeFrom.getRecordCount() - 1; i >= 0; i--) {
			fromRec = dataToMergeFrom.getRecordAtIndex(i);
			PvpRecord toRec = findMatchingRecord();

			if (toRec != null) {
				// toRec.getId() < matchedRecords.length : this test is here for the case where 2 new records match to each other.
				// In that case, we would add a record to the dataToMergeTo, and then we would match the second new record.
				// But the toRec.getId() would be bigger than the maxID when we started because the toRec is a new record
				if (toRec.getId() < matchedRecords.length) {
					processMatching(toRec);
				} else {
					processCopyNew();
				}

			} else {
				if (fromRec.getId() > maxIdMatching) {
					// this is a new record, because its ID is bigger than we know about
					processCopyNew();
				} else {
					if (USE_DELETE_UI) {
						// TEMP FOR DELETE UI : this is a new record, because its ID is bigger than we know about
						delRecs.add(new DelRec(fromRec, true));
					}
					// this record was deleted, because we didn't match on one we know about
					logRecInfo("> NOT adding a record Id:" + fromRec.getId() + ":" + maxIdMatching + " :: " + fromRec.getFullText(false));
				}
			}
		}

		for (int i2 = dataToMergeTo.getRecordCount() - 1; i2 >= 0; i2--) {
			PvpRecord r = dataToMergeTo.getRecordAtIndex(i2);
			if (r.getId() <= maxIdMatching && !matchedRecords[r.getId()]) {
				if (USE_DELETE_UI) {
					delRecs.add(new DelRec(r, false));
				}
				context.ui.notifyInfo("> deleting a record Id:" + r.getId() + " :: " + r.getFullText(false));
				dataToMergeTo.getRecords().remove(i2);
				r.setId(0);
				dirtyTo(true);
			} else if (r.getId() > dataToMergeFrom.getMaxId()) { // TODO test this some
				context.ui.notifyInfo("> adding record to FROM Id:" + r.getId() + ":" + r);
				dirtyFrom(true);
			}
		}

		if (thisStartingRecordCount != dataToMergeFrom.getRecordCount()) {
			context.ui.notifyInfo("> different record counts:" + thisStartingRecordCount + ":" + dataToMergeFrom.getRecordCount());
			dirtyFrom(true); // TODO not sure about this
		}

		if (USE_DELETE_UI) {
			context.registerMergeDeletes(delRecs);
		}

		context.ui.notifyInfo("> records not matched by index:" + indexNotMatching);
		context.ui.notifyInfo("> sameModDateCount:" + sameModDateCount);
		context.ui.notifyInfo("> resultState:" + resultState);
		context.ui.notifyInfo(">>>>>>>>>> end merge. Matched types:" + typesMatched + "  Matched Records:" + recordsMatched);
		return resultState;
	}

	/**
	 * Find record in dataToMergeTo that matches fromRec
	 */
	private PvpRecord findMatchingRecord() {
		int matchRating;

		if (i < dataToMergeTo.getRecordCount()) {
			final PvpRecord recAtIndex = dataToMergeTo.getRecordAtIndex(i);
			matchRating = fromRec.matchRating(recAtIndex);
			if (fromRec.getId() == recAtIndex.getId() && matchRating > 30) {
				if (matchRating < 100) {
					logRecInfo("> Matching By Index. matchRating:" + matchRating);
				}
				return recAtIndex;
			} else {
				indexNotMatching++;
			}
		}

		final PvpRecord recWithId = dataToMergeTo.getRecord(fromRec.getId());
		matchRating = fromRec.matchRating(recWithId);
		logRecInfo("> Matching By Id. matchRating:" + matchRating);
		if (matchRating > 30) {
			return recWithId;
		}

		logRecInfo("> looking for matching by toString");

		int bestMatchRating = -1;
		PvpRecord bestMatchRec = null;

		List<PvpRecord> recordsTS = dataToMergeTo.getRecordsByToString(fromRec.getType(), fromRec.toString());
		for (final PvpRecord rTS : recordsTS) {
			matchRating = fromRec.matchRating(rTS);
			if (matchRating > bestMatchRating) {
				bestMatchRating = matchRating;
				bestMatchRec = rTS;
			}
		}
		if (bestMatchRating > 50) {
			logRecInfo("> found matching ToString");
			return bestMatchRec;
		}

		return null;
	}

	/**
	 * Process a record when it matches an existing record.
	 */
	private void processMatching(PvpRecord toRec) {
		recordsMatched++;
		//if (toRec.getId() >= matchedRecords.length) {
		//	logRecInfo("> before indexOutOfRange length: " + matchedRecords.length + " :: " + toRec.getFullText(false));
		//}
		matchedRecords[toRec.getId()] = true;
		if (toRec.getId() == fromRec.getId() && toRec.getId() > maxIdMatching) {
			maxIdMatching = toRec.getId();
			logRecInfo("> maxIdMatching:" + maxIdMatching);
		}
		makeIdentical(toRec);
	}

	/**
	 * Process a record by copying it as a new record.
	 */
	private void processCopyNew() {
		int nextID = dataToMergeTo.getNextMaxId();
		logRecInfo("> adding a record Id:" + nextID + ":" + fromRec);
		fromRec.setId(nextID);
		dataToMergeTo.getRecords().add(fromRec);
		dirtyTo(true);
	}

	private void makeIdentical(final PvpRecord toRec) {
		Date toRMD = toRec.getModificationDate() == null ? ZERO_DATE : toRec.getModificationDate();
		Date fromRMD = fromRec.getModificationDate() == null ? ZERO_DATE : fromRec.getModificationDate();
		if (fromRMD.equals(toRMD)) {
			sameModDateCount++;
		} else if (fromRMD.after(toRMD)) {
			// the fromRec was modified later - assume we want that data
			boolean b = fromRec.copyTo(toRec);
			dirtyTo(b);
			if (b) {
				logRecInfo("> makeIdentical - dirtyTo");
			}
		} else {
			// we don't care about the copiedData, only what is returned
			boolean b = toRec.copyTo(fromRec);
			dirtyFrom(b);
			if (b) {
				logRecInfo("> makeIdentical - dirtyFrom");
			}
		}
	}

}
