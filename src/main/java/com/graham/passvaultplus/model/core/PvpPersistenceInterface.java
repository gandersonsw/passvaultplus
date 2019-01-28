/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.*;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.UserAskToChangeFileException;
import com.graham.passvaultplus.actions.ExportXmlFile;
import com.graham.passvaultplus.view.longtask.LTManager;
import com.graham.passvaultplus.view.longtask.LongTask;
import com.graham.passvaultplus.view.longtask.LongTaskNoException;

/**
 * All methods to load data into RtDataInterface from the file
 */
public class PvpPersistenceInterface {
		public enum SaveTrigger {
				quit, // the application is quiting
				cud,  // there was a Create, Update or Delete
				major // some major change
		}

		public static final String EXT_COMPRESS = "zip";
		public static final String EXT_ENCRYPT = "bmn";
		public static final String EXT_XML = "xml";
		public static final String EXT_BOTH = EXT_COMPRESS + "." + EXT_ENCRYPT;

		final private PvpContext context;
		final private List<PvpBackingStore> backingStores;
		private boolean errorHappened;

		public PvpPersistenceInterface(final PvpContext contextParam) {
				context = contextParam;
				backingStores = new ArrayList<>();
				backingStores.add(new PvpBackingStoreFile(context.prefs.getDataFile())); // File needs to be the first Backing Store in the list
				backingStores.add(new PvpBackingStoreGoogleDocs(context));
		}

		public static boolean isCompressed(final String path) {
				// path is .zip or .zip.bmn
				return path.endsWith("." + EXT_COMPRESS) || path.endsWith("." + EXT_BOTH);
		}

		public static boolean isEncrypted(final String path) {
				// path is .bmn or .zip.bmn
				return path.endsWith("." + EXT_ENCRYPT);
		}

		public static boolean isPvpFileExt(final String fileExtension) {
				return fileExtension.equals(EXT_COMPRESS) || fileExtension.equals(EXT_ENCRYPT) || fileExtension.equals(EXT_XML) || fileExtension.equals(EXT_BOTH);
		}

		public static String formatFileName(final String fnameWithExt, final boolean compressed, final boolean encrypted) {
				String fname = BCUtil.getFileNameNoExt(fnameWithExt, true);
				if (compressed) {
						fname = fname + "." + PvpPersistenceInterface.EXT_COMPRESS;
				}
				if (encrypted) {
						fname = fname + "." + PvpPersistenceInterface.EXT_ENCRYPT;
				}
				if (!compressed && !encrypted) {
						fname = fname + "." + PvpPersistenceInterface.EXT_XML;
				}
				return fname;
		}

		public static String convertFileExtensionToEnglish(final String fileExtension) {
				if (isCompressed(fileExtension)) {
						if (isEncrypted(fileExtension)) {
								return "Compressed and Encrypted";
						}
						else {
								return "Compressed only";
						}
				}
				else {
						if (isEncrypted(fileExtension)) {
								return "Encrypted only";
						}
						else {
								return "Not Compressed or Encrypted";
						}
				}
		}

		public List<PvpBackingStore> getEnabledBackingStores(boolean includeUnmodifiedRemotes) {
				List<PvpBackingStore> bsList = new ArrayList<>();
				for (PvpBackingStore bs : backingStores) {
						if (bs.isEnabled() && (includeUnmodifiedRemotes || !bs.isUnmodifiedRemote())) {
								bsList.add(bs);
						}
				}
				return bsList;
		}

		private void load(PvpDataInterface dataInterface) throws UserAskToChangeFileException, PvpException {
				context.ui.notifyInfo("PvpPersistenceInterface.load :: START");

				final List<PvpBackingStore> enabledBs = getEnabledBackingStores(false);

				for (PvpBackingStore bs : enabledBs) {
						bs.clearTransientData();
				}

				/*try {
						Thread.sleep(2000); // for testing
				} catch (InterruptedException e) {
						e.printStackTrace();
				} */

				// sort with newest first. When the merge is done, it will know that the newest data is loaded, and it it merging in older data
				PvpBackingStore[] sortedBSArr = new PvpBackingStore[enabledBs.size()];
				sortedBSArr = enabledBs.toArray(sortedBSArr);
				Arrays.sort(sortedBSArr, (bs1, bs2) -> Long.compare(bs2.getLastUpdatedDate(), bs1.getLastUpdatedDate()));

				boolean doMerge = false;
				boolean wasChanged = false; // TODO way to write out if it was changed?
				for (PvpBackingStore bs : sortedBSArr) {
						LTManager.nextStep("Loading data from: " + bs.getShortName());
						context.ui.notifyInfo("loading bs : " + bs.getClass().getName());
						context.ui.notifyInfo("update date: " + new Date(bs.getLastUpdatedDate()));
						final PvpInStreamer fileReader = new PvpInStreamer(bs, context);

						BufferedInputStream inStream = null;
						try {
								inStream = fileReader.getStream();
						}
						catch (UserAskToChangeFileException ucf) {
								throw ucf; // this is not a real exception, just a signal that we should go back to configuration options
						}
						catch (InvalidKeyException e) {
								context.ui.notifyInfo("at InvalidKeyException");
								bs.setException(new PvpException(PvpException.GeneralErrCode.InvalidKey, e));
								continue;
								//throw new PvpException(PvpException.GeneralErrCode.InvalidKey, e);
						}
						catch (Exception e) {
								context.ui.notifyWarning("PI load Exception: " + e); // TODO might delete this since the exceptin is hadnled by bs
								bs.setException(new PvpException(PvpException.GeneralErrCode.CantOpenDataFile, e).setAdditionalDescription(bs.getDisplayableResourceLocation()));
								continue;
								//throw new PvpException(PvpException.GeneralErrCode.CantOpenDataFile, e).setAdditionalDescription(getFileDesc(bs));
						}

						// TODO does fileReader.close(); need to be called if we dont get here?

						try {
								PvpDataInterface newDataInterface = DatabaseReader.read(context, inStream);
								if (doMerge) {
										if (dataInterface.mergeData(newDataInterface) != PvpDataMerger.MergeResultState.NO_CHANGE) {
												wasChanged = true;
										}
								}
								else {
										dataInterface.setData(newDataInterface);
										doMerge = true;
								}
								context.prefs.setEncryptionStrengthBits(fileReader.getAesBits());
								bs.setLoadState(PvpBackingStore.LoadState.loaded);
						}
						catch (UserAskToChangeFileException ucf) {
								throw ucf;
						}
						catch (Exception e) {
								context.ui.notifyInfo("at 37 Exception: " + e.getMessage());
								e.printStackTrace();
								bs.setException(new PvpException(PvpException.GeneralErrCode.CantParseXml, e).setOptionalAction(new ExportXmlFile(context, bs)).setAdditionalDescription(bs.getDisplayableResourceLocation()));
						}
						finally {
								// note that close is called 2 times when an error happens
								fileReader.close();
						}
				}

				if (!doMerge) { // if this is false, nothing was loaded -> we have a problem
						throw enabledBs.get(0).getException();
				}

				if (wasChanged) {
						// set them as dirty, so they will eventually save
						for (PvpBackingStore bs : getEnabledBackingStores(true)) {
								bs.setDirty(true);
						}

						context.ui.notifyInfo("PvpPersistenceInterface.load :: was changed is TRUE");
				}
				else {
						boolean wasErrA = false;
						for (PvpBackingStore bs : sortedBSArr) {
								if (bs.getException() != null) {
										wasErrA = true;
								}
						}
						context.ui.notifyInfo("PvpPersistenceInterface.load :: ready to call allStoresAreUpToDate. wasErrA: " + wasErrA);
						if (!wasErrA) {
								for (PvpBackingStore bs : getEnabledBackingStores(true)) {
										bs.allStoresAreUpToDate();
								}
						}
				}
		}

		public LongTask loadLT(PvpDataInterface dataInterface) {
				return new LongTaskLoad(dataInterface);
		}

		class LongTaskLoad implements LongTask {
				private PvpDataInterface dataInterface;
				public LongTaskLoad(PvpDataInterface dataInterfaceParam) {
						dataInterface = dataInterfaceParam;
				}
				public void runLongTask() throws Exception {
						load(dataInterface);
				}
			//	public void cancel() {
			//	}
		}

	/**
	 * return true to Quit. Return false to cancel Quit, and keep app running
	 */
	public boolean appQuiting() {
			// TODO test when cancel is pressed
		LTManager.runSync(saveLT(context.data.getDataInterface(), SaveTrigger.quit), "Saving...");
		//save(context.data.getDataInterface(), SaveTrigger.quit);
		return !errorHappened;
	}

		private void save(PvpDataInterface dataInterface, SaveTrigger saveTrig) {
				final List<PvpBackingStore> enabledBs = getEnabledBackingStores(true);
				errorHappened = false;

				for (PvpBackingStore bs : enabledBs) {
						bs.clearTransientData();
						switch (bs.getChattyLevel()) {
								case unlimited:
								case localLevel:
								case remoteHeavy:
										if (saveTrig == SaveTrigger.quit) {
												if (bs.isDirty()) {
														saveOneBackingStore(dataInterface, bs);
												}
										}
										else {
												saveOneBackingStore(dataInterface, bs);
										}
										break;
								case remoteMedium:
								case remoteLight:
								case mostRestricted:
										if (saveTrig == SaveTrigger.quit) {
												if (bs.isDirty()) {
														saveOneBackingStore(dataInterface, bs);
												}
										}
										else if (saveTrig == SaveTrigger.major) {
												saveOneBackingStore(dataInterface, bs);
										}
										else {
												bs.setDirty(true);
										}
										break;
						}
				}

				boolean allSaved = true;
				for (PvpBackingStore bs : enabledBs) {
						if (bs.isDirty()) {
								allSaved = false;
						}
				}
				context.ui.notifyInfo("PvpPersistenceInterface.save :: ready to call allStoresAreUpToDate. allSaved:" + allSaved);
				if (allSaved) {
						for (PvpBackingStore bs : enabledBs) {
								bs.allStoresAreUpToDate();
						}
				}
		}

		public LongTaskNoException saveLT(PvpDataInterface dataInterface, SaveTrigger saveTrig) {
			return new LongTaskSave(dataInterface, saveTrig);
		}

		class LongTaskSave implements LongTaskNoException {
				private PvpDataInterface dataInterface;
				private PvpPersistenceInterface.SaveTrigger saveTrig;
				public LongTaskSave(PvpDataInterface dataInterfaceParam, PvpPersistenceInterface.SaveTrigger saveTrigParam) {
						dataInterface = dataInterfaceParam;
						saveTrig = saveTrigParam;
				}
				public void runLongTask() {
						save(dataInterface, saveTrig);
				}
			//	public void cancel() {
			//	}
		}

		public void saveOneBackingStore(PvpDataInterface dataInterface, PvpBackingStore bs) {
				context.ui.notifyInfo("SAVING BACKING STORE:" + bs.getClass().getName());
				if (!bs.shouldBeSaved()) {
						context.ui.notifyInfo("this backing store will not be saved (probably because load failed):" + bs.getClass().getName());
						return;
				}
				/*try {
						Thread.sleep(7000); // for testing
				} catch (InterruptedException e) {
						e.printStackTrace();
				}*/
				LTManager.nextStep("Saving data to: " + bs.getShortName());
				try {
						if (bs.supportsFileUpload()) {
								bs.doFileUpload();
						} else {
								PvpOutStreamer fileWriter = null;
								try {
										fileWriter = new PvpOutStreamer(bs, context.prefs);
										DatabaseWriter.write(context, fileWriter.getWriter(), dataInterface);
								} finally {
										// note that close is called 2 times if Exception thrown from fileWriter.getWriter()
										if (fileWriter != null) {
												fileWriter.close();
										}
								}
						}
						bs.setDirty(false);
						bs.setLoadState(PvpBackingStore.LoadState.loaded); // set this because we want this BS to be treated as if it loaded successfully now
						bs.setException(null);
				} catch (Exception e) {
						errorHappened = true;
						context.ui.notifyBadException(e, true, PvpException.GeneralErrCode.CantWriteDataFile);
				}
		}

		public LongTaskNoException saveOneBackingStoreLT(PvpDataInterface dataInterface, PvpBackingStore bs) {
				return new LongTaskSaveOneBS(dataInterface, bs);
		}

		class LongTaskSaveOneBS implements LongTaskNoException {
				private PvpDataInterface dataInterface;
				private PvpBackingStore bs;
				public LongTaskSaveOneBS(PvpDataInterface dataInterfaceParam, PvpBackingStore bsParam) {
						dataInterface = dataInterfaceParam;
						bs = bsParam;
				}
				public void runLongTask() {
						saveOneBackingStore(dataInterface, bs);
				}
				//	public void cancel() {
				//	}
		}

}
