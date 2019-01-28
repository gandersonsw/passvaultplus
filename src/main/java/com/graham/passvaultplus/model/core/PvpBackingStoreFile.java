/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;

import com.graham.passvaultplus.AppUtil;

public class PvpBackingStoreFile extends PvpBackingStoreAbstract {

	private File theF;

	public PvpBackingStoreFile(File f) {
		theF = f;
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
		return new FileInputStream(theF);
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		checkBackupFileHourly(theF); // TODO - is this correct? if this called and save does not happen - bad things will happen
		return new FileOutputStream(theF);
	}

	@Override
	public boolean isCompressed(boolean inFlag) {
		return PvpPersistenceInterface.isCompressed(theF.getName());
	}

	@Override
	public boolean isEncrypted(boolean inFlag) {
		return PvpPersistenceInterface.isEncrypted(theF.getName());
	}

	/**
	 * Check to see if a backup file has been created in the last hour. If it has not, rename the given file to the backup file name.
	 */
	private void checkBackupFileHourly(final File f) {
		String filenameParts[] = AppUtil.getFileNameParts(f.getName());
		String timeStamp = AppUtil.getHourlyTimeStamp();
		File backupFile = new File(f.getParentFile(), filenameParts[0] + timeStamp + "." + filenameParts[1]);
		// don't backup if a backup has been done within the last hour
		if (!backupFile.exists()) {
			f.renameTo(backupFile);
		}
	}

	public File[] getAllFiles(boolean sortByDate) {
		File[] files = theF.getParentFile().listFiles(new MyFF(theF));
		if (sortByDate) {
			Arrays.sort(files, (f1, f2) -> {
				Date d1 = AppUtil.parseHourlyTimeStamp(f1.getName());
				Date d2 = AppUtil.parseHourlyTimeStamp(f2.getName());
				return d1.compareTo(d2);
			});
		}
		return files;
	}

	public void deleteAll() {
		File[] fArr = getAllFiles(false);
		for (File f : fArr) {
			System.out.println("PvpBackingStoreFile.deleteAll - deleteing:" + f);
			f.delete();
		}
	}

	class MyFF implements FilenameFilter {
		String filenameParts[];
		public MyFF(final File f) {
			filenameParts = AppUtil.getFileNameParts(f.getName());
		}
		public boolean accept(File dir, String name) {
			String testParts[] = AppUtil.getFileNameParts(name);
			return testParts[0].startsWith(filenameParts[0]) && PvpPersistenceInterface.isPvpFileExt(testParts[1]);
		}
	}

	@Override
	public long getLastUpdatedDate() {
		return theF.lastModified();
	}

	@Override
	public String getDisplayableResourceLocation() {
		return "File: " + theF;
	}

	@Override
	public String getShortName() {
		return "File";
	}

	@Override
	public void userAskedToHandleError() {
		// TODO
	}

}
