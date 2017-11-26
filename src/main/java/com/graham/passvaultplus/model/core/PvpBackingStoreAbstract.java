/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import com.graham.passvaultplus.PvpException;

public abstract class PvpBackingStoreAbstract implements PvpBackingStore {
	
	private boolean dirty;
	private PvpException exception;
	
	@Override
	public boolean supportsFileUpload() {
		return false;
	}
	
	@Override
	public void doFileUpload() {
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
		if (dirty) {
			System.out.println("Warning: dirty is true : " + this.getClass().getName());
		}
		// TODO , should dirty be cleared ?
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
