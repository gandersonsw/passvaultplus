/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.awt.Color;
import java.io.IOException;

import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.view.StatusBox;
import com.graham.passvaultplus.view.longtask.LTRunner;
import com.graham.util.StringUtil;

import javax.swing.*;

public abstract class PvpBackingStoreAbstract implements PvpBackingStore {
	private boolean dirty;
	protected volatile BsState bsState = BsState.StartState;
	private PvpException exception;
	private StatusBox statusBox;

	@Override
	public boolean supportsFileUpload() {
		return false;
	}

	@Override
	public void doFileUpload(LTRunner ltr) throws IOException, PvpException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void setDirty(final boolean dirtyParam) {
		if (dirty != dirtyParam) {
			dirty = dirtyParam;
			updateStatusBox();
		}
	}

	@Override
	public BsState getBsState() {
		return bsState;
	}

	@Override
	public void stateTrans(BsStateTrans trans) {
			boolean badStateTran = false;
			BsState newState;
			switch (trans) {
					case StartLoading:
							newState = BsState.Loading;
							if (bsState == BsState.Loading || bsState == BsState.Saving) {
									badStateTran = true;
							} else {
									exception = null; // clear the exception because we are starting new
							}
							break;
					case StartSaving:
							newState = BsState.Saving;
							if (bsState == BsState.StartState || bsState == BsState.Loading || bsState == BsState.Saving || bsState == BsState.ErrorLoading) {
									badStateTran = true;
							} else {
									exception = null; // clear the exception because we are starting new
							}
							break;
					case EndLoading:
							if (bsState == BsState.ErrorLoading) {
									return;
							}
							if (bsState == BsState.StartState || bsState == BsState.Saving || bsState == BsState.ErrorSaving) {
									badStateTran = true;
							}
							newState = exception == null ? BsState.AllGood : BsState.ErrorLoading;
							break;
					case EndSaving:
							if (bsState == BsState.ErrorSaving) {
									return;
							}
							if (bsState == BsState.StartState || bsState == BsState.Loading || bsState == BsState.ErrorLoading) {
									badStateTran = true;
							}
							newState = exception == null ? BsState.AllGood : BsState.ErrorSaving;
							break;
					case initSave:
							if (bsState == BsState.Loading || bsState == BsState.Saving) {
									badStateTran = true;
							}
							newState = BsState.Saving;
							break;
					default:
							throw new IllegalArgumentException("unknown BsStateTrans:" + trans);
			}

			if (badStateTran) {
					throw new IllegalArgumentException("cannot go from " + bsState + " to " + newState + " :: " + this.getClass().getName());
			}
			//System.out.println(this.getClass().getName() +  " :STATE TRAN: "+ bsState + " to " + newState);
			bsState = newState;
			if (trans == BsStateTrans.EndLoading || trans == BsStateTrans.EndSaving) {
					updateStatusBox();
			}
	}

	@Override
	public void clearTransientData() {
		exception = null;
		// dirty should not be cleared. When loading, dirty is set to true if the BackingStores
		// are out of sync, and the flag will signal that they should be saved
	}

	@Override
	public void setException(PvpException e) {
		if (e == null) {
			throw new NullPointerException("exception cannot be null.");
		}
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
		SwingUtilities.invokeLater(() -> {
			if (statusBox != null) {
				if (exception != null) {
					statusBox.setColor(Color.ORANGE);
					statusBox.setToolTipText("Error:" + getErrorMessageForDisplay());
				} else if (dirty) {
					statusBox.setColor(new Color(79, 138, 255));
					statusBox.setToolTipText("Not stored yet. Click to store now.");
				} else {
					statusBox.setColor(Color.GREEN);
					statusBox.setToolTipText("All data stored" + (this.getChattyLevel().isRemote() ? ". Click to check for changes." : ""));
				}
			}
		});
	}

	protected String getErrorMessageForDisplay() {
		if (StringUtil.stringEmpty(exception.getMessage())) {
			return exception.getPvpErrorTitle();
		}
		return exception.getMessage();
	}

	@Override
	public void allStoresAreUpToDate(LTRunner ltr) {
		// by default, don't do anything
	}

	@Override
	public boolean isUnmodifiedRemote(LTRunner ltr) {
		return false;
	}

}
