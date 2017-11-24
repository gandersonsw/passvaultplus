/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.graham.passvaultplus.PvpContext;

public class PvpBackingStoreFile extends PvpBackingStoreAbstract {
	
	private final PvpContext context;

	public PvpBackingStoreFile(PvpContext contextParam) {
		context = contextParam;
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
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(context.getDataFile());
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return new FileOutputStream(context.getDataFile());
	}

}
