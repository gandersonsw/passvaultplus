/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	public InputStream openInputStream() throws IOException {
		return new FileInputStream(f);
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return new FileOutputStream(f);
	}

	@Override
	public boolean isCompressed(boolean inFlag) {
		return PvpPersistenceInterface.isCompressed(f.getName());
	}
	
	@Override
	public boolean isEncrypted(boolean inFlag) {
		return PvpPersistenceInterface.isEncrypted(f.getName());
	}

	@Override
	public long getLastUpdatedDate() {
		return f.lastModified();
	}
	
	@Override
	public String getDisplayableResourceLocation() {
		return "File: " + f;
	}
	
	@Override
	public String getShortName() {
		return "Other File";
	}

}
