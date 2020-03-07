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

import com.graham.util.DateUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.util.FileUtil;

public class PvpBackingStoreFile extends PvpBackingStoreAbstract {

	private final File theF;
	private final PvpContext context;

	public PvpBackingStoreFile(PvpContext c) {
		context = c;
		theF = null;
	}

	public PvpBackingStoreFile(File f) {
		context = null;
		theF = f;
	}

	private File getFile() {
			if (theF == null) {
					return context.prefs.getDataFile();
			} else {
					return theF;
			}
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
		return new FileInputStream(getFile());
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		checkBackupFileHourly(); // TODO - is this correct? if this called and save does not happen - bad things will happen
		return new FileOutputStream(getFile());
	}

	@Override
	public boolean isCompressed(boolean inFlag) {
		return PvpPersistenceInterface.isCompressed(getFile().getName());
	}

	@Override
	public boolean isEncrypted(boolean inFlag) {
		return PvpPersistenceInterface.isEncrypted(getFile().getName());
	}

	/**
	 * Check to see if a backup file has been created in the last hour. If it has not, rename the given file to the backup file name.
	 */
	private void checkBackupFileHourly() {
		File df = getFile();
		String filenameParts[] = FileUtil.getFileNameParts(df.getName());
		String timeStamp = DateUtil.getHourlyTimeStamp();
		File backupFile = new File(df.getParentFile(), filenameParts[0] + timeStamp + "." + filenameParts[1]);
		// don't backup if a backup has been done within the last hour
		if (!backupFile.exists()) {
			df.renameTo(backupFile);
		}
	}

	public File[] getAllFiles(boolean sortByDate) {
		File df = getFile();
		File[] files = df.getParentFile().listFiles(new MyFF(df));
		if (sortByDate) {
			Arrays.sort(files, (f1, f2) -> {
				Date d1 = DateUtil.parseHourlyTimeStamp(f1.getName());
				Date d2 = DateUtil.parseHourlyTimeStamp(f2.getName());
				return d1.compareTo(d2);
			});
		}
		return files;
	}

	public void deleteAll() {
		File[] fArr = getAllFiles(false);
		for (File f : fArr) {
			context.ui.notifyInfo("PvpBackingStoreFile.deleteAll - deleteing:" + f);
			f.delete();
		}
	}

	class MyFF implements FilenameFilter {
		String filenameParts[];
		public MyFF(final File f) {
			filenameParts = FileUtil.getFileNameParts(f.getName());
		}
		public boolean accept(File dir, String name) {
			String testParts[] = FileUtil.getFileNameParts(name);
			return testParts[0].startsWith(filenameParts[0]) && PvpPersistenceInterface.isPvpFileExt(testParts[1]);
		}
	}

	@Override
	public long getLastUpdatedDate() {
		return getFile().lastModified();
	}

	@Override
	public String getDisplayableResourceLocation() {
		return "File: " + getFile();
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
