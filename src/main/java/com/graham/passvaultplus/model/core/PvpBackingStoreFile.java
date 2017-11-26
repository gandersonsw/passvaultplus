/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.graham.passvaultplus.AppUtil;
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
	public InputStream openInputStream() throws IOException {
		return new FileInputStream(context.getDataFile());
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		checkBackupFileHourly(context.getDataFile()); // TODO - is this correct? if this called and save does not happen - bad things will happen
		return new FileOutputStream(context.getDataFile());
	}
	
	@Override
	public boolean isCompressed(boolean inFlag) {
		return PvpPersistenceInterface.isCompressed(context.getDataFile().getName());
	}
	
	@Override
	public boolean isEncrypted(boolean inFlag) {
		return PvpPersistenceInterface.isEncrypted(context.getDataFile().getName());
	}
	
	
	/**
	 * Check to see if a backup file has been created in the last hour. If it has not, rename the given file to the backup file name.
	 */
	private void checkBackupFileHourly(final File f) {
		String filenameParts[] = AppUtil.getFileNameParts(f.getName());
		String timeStamp = AppUtil.getHourlyTimeStamp();
		File backupFile = new File(f.getParentFile(), filenameParts[0] + "-" + timeStamp + "." + filenameParts[1]);
		// don't backup if a backup has been done within the last hour
		if (! backupFile.exists()) {
			f.renameTo(backupFile);
		}
	}

	@Override
	public long getLastUpdatedDate() {
		return context.getDataFile().lastModified();
	}
	
	@Override
	public String getDisplayableResourceLocation() {
		return "File: " + context.getDataFile();
	}

}
