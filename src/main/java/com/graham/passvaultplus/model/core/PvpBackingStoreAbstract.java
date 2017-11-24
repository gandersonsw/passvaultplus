/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

public abstract class PvpBackingStoreAbstract implements PvpBackingStore {
	
	private boolean dirty;
	
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
	
}
