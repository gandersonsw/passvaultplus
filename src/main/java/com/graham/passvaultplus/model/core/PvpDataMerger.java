/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.graham.passvaultplus.PvpContext;

/**
 * Merge 2 datasets. The TO dataset is considered the master.
 * NO_CHANGE    -> means no serialized of datasets needed
 * FROM_CHANGED -> means the FROM dataset should be overwritten with the new TO dataset
 * TO_CHANGED   -> means the TO dataset should be written
 * BOTH_CHANGED -> means both datasets should be serialized using the TO dataset.
 *
 * Both datasets should be identical, so the TO and FROM should be the same at the end (after serialization)
 */
public class PvpDataMerger {

	public static class DelRec {
		final public int id;
		final public String txt;
		final public boolean inFromDb;
		public DelRec(PvpRecord r, boolean inFromDbParam) {
			id = r.getId();
			txt = r.getFullText(false);
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
	private MergeResultState resultState = MergeResultState.NO_CHANGE;
	private PvpRecord fromRec;
	private int curLogInofId;
	private int i;
	private int sameModDateCount;

	public PvpDataMerger(final PvpContext contextParam) {
		context = contextParam;
	}

	/**
	 * if param is true, add the "From" PvpDataInterface to the ones that are
	 * dirty (that have been modified)
	 */
	private void dirtyFrom(boolean b) {
		if (b) {
			//new Exception().printStackTrace();
			resultState = MergeResultState.getByBits(resultState.bits | MergeResultState.FROM_CHANGED.bits);
		}
	}

	/**
	 * if param is true, add the "To PvpDataInterface to the ones that are dirty
	 * (that have been modified)
	 */
	private void dirtyTo(boolean b) {
		if (b) {
			//new Exception().printStackTrace();
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

	/**
	 * dataToMergeTo is considered the newer dataset. dataToMergeFrom is considered the older dataset.
	 */
	public MergeResultState mergeData(PvpDataInterface dataToMergeTo, PvpDataInterface dataToMergeFrom) {

		context.ui.notifyInfo(">>>>>>>>>> start merge. toMaxId:" + dataToMergeTo.getMaxId() + " fromMaxId:" + dataToMergeFrom.getMaxId());

		int thisStartingRecordCount = dataToMergeTo.getRecordCount();
		int maxIdMatching = 0; // the largest ID that existed in both databases that match
		List<DelRec> delRecs = new ArrayList<>();

		// IDs of records in the main database that have a corresponding record in the mergeFrom database
		boolean[] matchedRecords = new boolean[dataToMergeTo.getMaxId() + 1];
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
				// Since we don't track the modification date for types, there is not a way to hadnle this for now.
				typesMatched++;
			}
		}
		// Since we do not track modified date for types, assume this was a type delete
		//		if (typesMatched < toTypeCount) {
		//			context.ui.notifyInfo("> typesMatched < dataToMergeTo.getTypes().size()");
		//			dirtyFrom(true);
		//		}

		int indexNotMatching = 0;

		int recordsMatched = 0;
		for (i = dataToMergeFrom.getRecordCount() - 1; i >= 0; i--) {
			fromRec = dataToMergeFrom.getRecordAtIndex(i);
			//if ( i < 3) {
			//		delRecs.add(new DelRec(fromRec, false));
			//}
			//context.ui.notifyInfo("> Trying to match Record:" + fromRec + " ----------");
			PvpRecord toRec = null;
			if (i < dataToMergeTo.getRecordCount()) {
				final PvpRecord recAtIndex = dataToMergeTo.getRecordAtIndex(i);
				if (fromRec.getId() == recAtIndex.getId()) { // && isMatchingRecord(recWithId, fromRec) ???
					toRec = recAtIndex;
				} else {
					indexNotMatching++;
				}
			}
			if (toRec == null) {
				final PvpRecord recWithId = dataToMergeTo.getRecord(fromRec.getId());
				logRecInfo("> Matching By Id. matchRating:" + fromRec.matchRating(recWithId));
				if (fromRec.matchRating(recWithId) > 30) {
					toRec = recWithId;
				}
			}
			if (toRec == null) {
				logRecInfo("> looking for matching by toString");
				List<PvpRecord> recordsTS = dataToMergeTo.getRecordsByToString(fromRec.getType(), fromRec.toString());
				for (final PvpRecord rTS : recordsTS) {
					if (fromRec.matchRating(rTS) > 50) {
						logRecInfo("> found matching ToString");
						toRec = rTS;
						break;
					}
				}
			}
			if (toRec != null) {
				recordsMatched++;
				if (toRec.getId() >= matchedRecords.length) {
					logRecInfo("> before indexOutOfRange length: " + matchedRecords.length + " :: " + toRec.getFullText(false));
				}
				matchedRecords[toRec.getId()] = true;
				if (toRec.getId() == fromRec.getId() && toRec.getId() > maxIdMatching) {
					maxIdMatching = toRec.getId();
					logRecInfo("> maxIdMatching:" + maxIdMatching);
				}
				makeIdentical(toRec);
			} else {
				if (fromRec.getId() > maxIdMatching) {
					// this is a new record, because its ID is bigger than we know about
					int nextID = dataToMergeTo.getNextMaxId();
					logRecInfo("> adding a record Id:" + nextID);
					fromRec.setId(nextID);
					dataToMergeTo.getRecords().add(fromRec);
					dirtyTo(true);
				} else {
					if (USE_DELETE_UI) {
						// TEMP FOR DELETE UI : this is a new record, because its ID is bigger than we know about
						int nextID = dataToMergeTo.getNextMaxId();
						logRecInfo("> adding a record Id (for DELETE UI):" + nextID);
						fromRec.setId(nextID);
						dataToMergeTo.getRecords().add(fromRec);
						dirtyTo(true);
						delRecs.add(new DelRec(fromRec, true));
					}
					// this record was deleted, because we didn't match on one we know about
					logRecInfo("> NOT adding a record Id:" + fromRec.getId() + ":" + maxIdMatching);
				}
			}
		}

		for (int i2 = dataToMergeTo.getRecordCount() - 1; i2 >= 0; i2--) {
			PvpRecord r = dataToMergeTo.getRecordAtIndex(i2);
			if (r.getId() <= maxIdMatching && !matchedRecords[r.getId()]) {
					if (USE_DELETE_UI) {
							delRecs.add(new DelRec(r, false));
					} else {
							context.ui.notifyInfo("> deleting a record Id:" + r.getId() + ":" + r);
							dataToMergeTo.getRecords().remove(i2);
							r.setId(0);
							dirtyTo(true);
					}
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

}
