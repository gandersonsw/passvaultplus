/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
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
 * Merge 2 datasets. The "TO" dataset is considered the master.
 * NO_CHANGE    -> means no serilized of datasets needed
 * FROM_CHANGED -> means the FROM dataset should be overwitten with the new "TO" dataset
 * TO_CHANGED   -> means the TO dataset should be written
 * BOTH_CHANGED -> means both datasets shold be serilized using the TO dataset.
 *
 * Both datasets should be identical, so the TO and FROM should be the same at the end (after serilization)
 */
public class PvpDataMerger {

  static public enum MergeResultState {
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
        case 0: return NO_CHANGE;
        case 1: return FROM_CHANGED;
        case 2: return TO_CHANGED;
        case 3: return BOTH_CHANGED;
      }
      throw new RuntimeException("invalid bitsParam");
    }
  }

  private final PvpContext context;
  private MergeResultState resultState = MergeResultState.NO_CHANGE;

	public PvpDataMerger(final PvpContext contextParam) {
		context = contextParam;
	}

  /**
   * if param is true, add the "From" PvpDataInterface to the ones that are dirty (that have been modified)
   */
  private void dirtyTo(boolean b) {
    if (b) {
      resultState = MergeResultState.getByBits(resultState.bits | MergeResultState.FROM_CHANGED.bits);
    }
  }

  /**
   * if param is true, add the "To PvpDataInterface to the ones that are dirty (that have been modified)
  */
  private void dirtyFrom(boolean b) {
    if (b) {
      resultState = MergeResultState.getByBits(resultState.bits | MergeResultState.TO_CHANGED.bits);
    }
  }

  private void makeIdentical(final PvpRecord existingRec, final PvpRecord newRec) {
    if (existingRec.getModificationDate() != null && newRec.getModificationDate() != null) {
      if (newRec.getModificationDate().after(existingRec.getModificationDate())) {
        // the newRec was modified later - assume we want that data
        boolean b = newRec.copyTo(existingRec);
        dirtyFrom(b);
        if (b) {
          context.notifyInfo("makeIdentical - dirtyTo");
        }
      } else {
        boolean b = existingRec.copyTo(newRec); // we don't care about the copiedData, only what is returned
        dirtyTo(b);
        if (b) {
          context.notifyInfo("makeIdentical - dirtyFrom");
        }
      }
    } else {
      context.notifyInfo("makeIdentical skipped because modification date missing:" + existingRec);
    }
  }

  public MergeResultState mergeData(PvpDataInterface dataToMergeTo, PvpDataInterface dataToMergeFrom) {

		context.notifyInfo(">>>>>> start merge. curMax:" + dataToMergeTo.getMaxId() + " mergeMaxId:" + dataToMergeFrom.getMaxId());

		int thisStartingRecordCount = dataToMergeTo.getRecordCount();
		int maxIdMatching = 0; // the largest ID that existed in both databases that match
		boolean[] matchedRecords = new boolean[dataToMergeTo.getMaxId()+1]; // IDs of records in the main database that have a corresponding record in the mergeFrom database
		int typesMatched = 0;
		//if (dataToMergeTo.getMaxId() != dataToMergeFrom.getMaxId()) {
			//wasChanged = true;
			//if (dataTocMergeFrom.maxID > maxID) {
			//	maxID = dataTocMergeFrom.maxID;
			//}
		//}

		for (PvpType newType : dataToMergeFrom.getTypes()) {
			PvpType existingType = dataToMergeTo.getType(newType.getName());
			if (existingType == null) {
				context.notifyInfo("adding type:" + newType.getName());
				dataToMergeTo.getTypes().add(newType);
        dirtyTo(true); // TODO verify this
			} else {
				// TODO compare and modify
				typesMatched++;
			}
		}

		int recordsMatched = 0;
		for (int i = dataToMergeFrom.getRecordCount() - 1; i >= 0; i--) {
			PvpRecord newRec = dataToMergeFrom.getRecordAtIndex(i);
			context.notifyInfo("--------- Trying to match Record:" + newRec + " ----------");
			PvpRecord existingRec = null;
			if (i < dataToMergeTo.getRecordCount()) {
				final PvpRecord recAtIndex = dataToMergeTo.getRecordAtIndex(i);
				if (newRec.getId() == recAtIndex.getId()) { // && isMatchingRecord(recWithId, newRec)  ???
					existingRec = recAtIndex;
				} else {
					context.notifyInfo("id did not match by index:" + newRec + ": " + existingRec);
				}
			}
			if (existingRec == null) {
				final PvpRecord recWithId = dataToMergeTo.getRecord(newRec.getId());
				if (newRec.matchRating(recWithId) > 30) {
					existingRec = recWithId;
				}
			}
			if (existingRec == null) {
				context.notifyInfo("looking for matching by toString");
				List<PvpRecord> recordsTS = dataToMergeTo.getRecordsByToString(newRec.getType(), newRec.toString());
				for (final PvpRecord rTS : recordsTS) {
					if (newRec.matchRating(rTS) > 50) {
						context.notifyInfo("found matching ToString");
						existingRec = rTS;
						break;
					}
				}
			}
			if (existingRec != null) {
				recordsMatched++;
				matchedRecords[existingRec.getId()] = true;
				if (existingRec.getId() == newRec.getId() && existingRec.getId() > maxIdMatching) {
					maxIdMatching = existingRec.getId();
					context.notifyInfo("maxIdMatching:" + maxIdMatching);
				}
				context.notifyInfo("at 77");
				makeIdentical(existingRec, newRec);
			} else {
				if (newRec.getId() > maxIdMatching) {
					// this is a new record, because its ID is bigger than we know about
					int nextID = dataToMergeTo.getNextMaxId();
					context.notifyInfo("adding a record:" + nextID + ":" + newRec);
					newRec.setId(nextID);
					dataToMergeTo.getRecords().add(newRec);
          dirtyTo(true);
				} else {
					// this record was deleted, because we didn't match on one we know about
					context.notifyInfo("NOT adding a record:" + newRec.getId() + ":" + maxIdMatching + ":" + newRec);
				}
			}
		}

		for (int i = dataToMergeTo.getRecordCount() - 1; i >= 0; i--) {
			PvpRecord r = dataToMergeTo.getRecordAtIndex(i);
			if (r.getId() <= maxIdMatching && !matchedRecords[r.getId()]) {
				context.notifyInfo("deleting a record:" + r.getId() + ":" + r);
				dataToMergeTo.getRecords().remove(i);
				r.setId(0);
        dirtyTo(true);
			}
		}

		if (thisStartingRecordCount != dataToMergeFrom.getRecordCount()) {
			context.notifyInfo("different record counts:" + thisStartingRecordCount + ":" + dataToMergeFrom.getRecordCount());
      resultState = MergeResultState.BOTH_CHANGED; // TODO not sure about this
		}

		context.notifyInfo(">>>>>> end merge. Matched types:" + typesMatched + "  Matched Records:" + recordsMatched);
		return resultState;
  }

}
