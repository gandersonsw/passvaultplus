/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.awt.Color;
import java.io.IOException;

import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.view.StatusBox;

public abstract class PvpBackingStoreAbstract implements PvpBackingStore {
	private boolean dirty;
	private LoadState loadState = LoadState.startState;
	PvpException exception;
	private StatusBox statusBox;

	@Override
	public boolean supportsFileUpload() {
		return false;
	}

	@Override
	public void doFileUpload() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void setDirty(final boolean dirtyParam) {
		dirty = dirtyParam;
		updateStatusBox();
	}

	@Override
	public PvpBackingStore.LoadState getLoadState() {
		return loadState;
	}

	@Override
	public void setLoadState(final PvpBackingStore.LoadState loadStateParam) {
		loadState = loadStateParam;
	}

	@Override
	public boolean shouldBeSaved() {
		return loadState != PvpBackingStore.LoadState.error;
	}

	@Override
	public void clearTransientData() {
		exception = null;
		// dirty should not be cleared. When loading, dirty is set to true if the BackingStores
		// are out of sync, and the flag will signal that they should be saved
		updateStatusBox();
	}

	@Override
	public void setException(PvpException e) {
		exception = e;
		updateStatusBox();
	}

	@Override
	public PvpException getException() {
		return exception;
	}

	@Override
	public void setStatusBox(StatusBox sb) {
		statusBox = sb;
		updateStatusBox();
	}

	@Override
	public StatusBox getStatusBox() {
		return statusBox;
	}

	private void updateStatusBox() {
		if (statusBox != null) {
			if (exception != null) {
				statusBox.setColor(Color.ORANGE);
				statusBox.setToolTipText("Error:" + getErrorMessageForDisplay());
			} else if (dirty) {
				statusBox.setColor(Color.BLUE.brighter());
				statusBox.setToolTipText("Not stored yet. Click to store now.");
			} else {
				statusBox.setColor(Color.GREEN);
				statusBox.setToolTipText("All data stored");
			}
		}
	}

	String getErrorMessageForDisplay() {
		exception.printStackTrace();
		return exception.getMessage();

	}

	public void allStoresAreUpToDate() {
		// by default, don't do anything
	}

	public boolean isUnmodifiedRemote() {
		return false;
	}

}
