/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.view.StatusBox;

/**
 * Interface for anything we use to interface to a backing storage. Could be file system,
 * or some server, or anything that will save out data for when we
 * want to use later.
 */
public interface PvpBackingStore {

	enum ChattyLevel {
		/**
		 * This level would be used for something like in memory data.
		 * A very fast backing store.
		 *
		 * Any requested change to data fires a save. (for example if
		 * users is typing is a filed, it is not saved, but every time
		 * they move to a different field or enter a large amount of
		 * text, a save may be triggered. or every ~30 seconds if there are changes)
		 */
		unlimited,

		/**
		 * This level would be used for something like a local file system.
		 * Where there is some latency, but not much cost for persistence
		 * reading "larger" (around 1MB) amounts of data.
		 *
		 * Saves happen when a record is saved. Or if a record is deleted.
		 * Very similar to a REST type where CRUD requests cause a data save.
		 */
		localLevel,

		/**
		 * A fast remote storage, where user does not need to worry about data usage limits/charges.
		 *
		 * Currently behaves the same as "localLevel"
		 */
		remoteHeavy,

		/**
		 * A fast/fairly fast remote storage, where user does not need to worry about data usage limits/charges.
		 *
		 * Writes will be done in bulk to some extent.
		 */
		remoteMedium,

		/**
		 * A medium or slow remote storage, where there may be light usage data limits/charges.
		 *
		 * Writes are often done in bulk. Maybe the most frequent would be once every ~30 minutes
		 */
		remoteLight,

		/**
		 * A slow remote storage or where there may be usage data limits/charges.
		 *
		 * A read is done once when starting app. Write is done once when quitting
		 * (or once every 24 hours if there have been changes), only if changes have been made.
		 */
		mostRestricted
	}

	// Backing Store State
	enum BsState {
		StartState,  // load has not been attempted
		AllGood,     // this BS was loaded
		ErrorSaving, // there was an error preventing this BS from saving
		ErrorLoading,// there was an error preventing this BS from loading
		Loading,		// A task is currently loading this data
		Saving			// A task is currently saving this data
	}

	// Backing Store State Transition
	enum BsStateTrans {
		StartLoading,
		StartSaving,
		EndLoading,
		EndSaving,
			initSave // TODO maybe delete this
	}

	ChattyLevel getChattyLevel();

	boolean isEnabled();

	InputStream openInputStream() throws IOException;

	OutputStream openOutputStream() throws IOException;

	boolean supportsFileUpload();

	void doFileUpload() throws IOException;

	boolean isDirty();
	void setDirty(boolean dirty);

	BsState getBsState();
	void stateTrans(BsStateTrans trans);
	boolean shouldBeSaved();

	boolean isCompressed(boolean inFlag);

	boolean isEncrypted(boolean inFlag);

	/**
	 * Call this before doing a group of work.
	 *
	 * For example, when a load operation is done, the BackingStore will cache some information.
	 * If another load is done like 60 minutes later, call this before starting the group of
	 * work, so that stale data is cleared out.
	 */
	void clearTransientData();

	/**
	 * @return If an error, return Long.MAX_VALUE
	 */
	long getLastUpdatedDate();

	void setException(PvpException e);

	PvpException getException();

	String getDisplayableResourceLocation();

	String getShortName();

	void setStatusBox(StatusBox sb);
	StatusBox getStatusBox();

	void userAskedToHandleError();

	/**
	 * todo
	 */
	void allStoresAreUpToDate();

	/**
	 * todo
	 */
	boolean isUnmodifiedRemote();

}
