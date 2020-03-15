/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.graham.passvaultplus.view.longtask.LTRunner;

public class PvpBackingStoreOtherFile extends PvpBackingStoreAbstract {
	
	private final File f;

	public PvpBackingStoreOtherFile(File fParam) {
		f = fParam;
	}
	
	@Override
	public ChattyLevel getChattyLevel() {
		return PvpBackingStore.ChattyLevel.localLevel;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public InputStream openInputStream(LTRunner ltr) throws IOException {
		return new FileInputStream(f);
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return new FileOutputStream(f);
	}

	@Override
	public boolean isCompressed(LTRunner ltr, boolean inFlag) {
		return PvpPersistenceInterface.isCompressed(f.getName());
	}
	
	@Override
	public boolean isEncrypted(LTRunner ltr, boolean inFlag) {
		return PvpPersistenceInterface.isEncrypted(f.getName());
	}

	@Override
	public long getLastUpdatedDate(LTRunner ltr) {
		return f.lastModified();
	}

	@Override
	public String getDisplayableResourceLocation(LTRunner ltr) {
		return "File: " + f;
	}
	
	@Override
	public String getShortName() {
		return "Other File";
	}
	
	@Override
	public void userAskedToHandleError(LTRunner ltr) {
		// TODO
	}

}
