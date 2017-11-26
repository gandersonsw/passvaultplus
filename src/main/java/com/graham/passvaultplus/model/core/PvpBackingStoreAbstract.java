/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.IOException;

import com.graham.passvaultplus.PvpException;

public abstract class PvpBackingStoreAbstract implements PvpBackingStore {
	
	private boolean dirty;
	private PvpException exception;
	
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
	}
	
	@Override
	public void clearTransientData() {
		exception = null;
		// dirty should not be cleared. When loading, dirty is set to true if the BackingStores 
		// are out of sync, and the flag will signal that they should be saved
	}
	
	@Override
	public void setException(PvpException e) {
		exception = e;
	}
	
	@Override
	public PvpException getException() {
		return exception;
	}
	
}
