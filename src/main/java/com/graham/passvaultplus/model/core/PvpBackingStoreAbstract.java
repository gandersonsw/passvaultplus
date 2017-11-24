/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

public abstract class PvpBackingStoreAbstract implements PvpBackingStore {
	
	private boolean dirty;
	
	public boolean isDirty() {
		return dirty;
	}
	
	public void setDirty(final boolean dirtyParam) {
		dirty = dirtyParam;
	}

}
