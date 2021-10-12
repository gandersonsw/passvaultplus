/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.*;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpContextUI;
import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.UserAskToChangeFileException;
import com.graham.passvaultplus.actions.ExportXmlFile;
import com.graham.passvaultplus.model.gdocs.PvpBackingStoreGoogleDocs;
import com.graham.passvaultplus.view.longtask.LTRunner;
import com.graham.passvaultplus.view.longtask.LongTask;
import com.graham.passvaultplus.view.longtask.LongTaskNoException;
import com.graham.util.FileUtil;

import javax.swing.*;

/**
 * All methods to load data into PvpDataInterface from the file
 */
public class PvpPersistenceInterface {
	public enum SaveTrigger {
		quit, // the application is quiting
		cud,  // there was a Create, Update or Delete
		major, // some major change
		init, // TODO maybe delete this
		metadata // there was a change to the metadata
	}

	public static final String EXT_COMPRESS = "zip"; // TODO delete - replace with .pvp
	public static final String EXT_ENCRYPT = "bmn"; // TODO delete - replace with .pvp
	public static final String EXT_XML = "xml";
	public static final String EXT_BOTH = EXT_COMPRESS + "." + EXT_ENCRYPT;

	final private PvpContext context;
	private PvpBackingStoreFile backingStoreFile;
	private List<PvpBackingStore> backingStores;
	private boolean errorHappened;

	public PvpPersistenceInterface(final PvpContext contextParam) {
		context = contextParam;
	}

	public static boolean isEncrypted(final String path) {
		// path is .bmn or .zip.bmn
		return path.endsWith("." + EXT_ENCRYPT);
	}

	public static boolean isPvpFileExt(final String fileExtension) {
		// fileExtension.equals(EXT_COMPRESS) || 
				return fileExtension.equals(EXT_ENCRYPT) || fileExtension.equals(EXT_XML) || fileExtension.equals(EXT_BOTH);
		}

	public static String formatFileName(final String fnameWithExt, final boolean encrypted) {
		String fname = FileUtil.getFileNameNoExt(fnameWithExt, true);
		if (encrypted) {
			//fname = fname + "." + PvpPersistenceInterface.EXT_COMPRESS;
			fname = fname + "." + PvpPersistenceInterface.EXT_ENCRYPT;
		} else {
			fname = fname + "." + PvpPersistenceInterface.EXT_XML;
		}
		return fname;
	}

	public static String convertFileExtensionToEnglish(final String fileExtension) {
		if (isEncrypted(fileExtension)) {
			return "Compressed and Encrypted";
		} else {
			return "Not Compressed or Encrypted";
		}
	}

	public List<PvpBackingStore> getEnabledBackingStoresWithUnmodifiedRemotes() {
		// note - call with Null LTRunner here - but we know that it is not used since includeUnmodifiedRemotes is true
		return getEnabledBackingStores(null, true);
	}

	public List<PvpBackingStore> getEnabledBackingStoresWithoutUnmodifiedRemotes(LTRunner ltr) {
		return getEnabledBackingStores(ltr, false);
	}

	private List<PvpBackingStore> getEnabledBackingStores(LTRunner ltr, boolean includeUnmodifiedRemotes) {
		if (backingStores == null) {
			backingStores = new ArrayList<>();
			backingStores.add(getBackingStoreFile()); // Very important - File needs to be the first Backing Store in the list
			backingStores.add(new PvpBackingStoreGoogleDocs(context));
		}
		List<PvpBackingStore> bsList = new ArrayList<>();
		for (PvpBackingStore bs : backingStores) {
			if (bs.isEnabled() && (includeUnmodifiedRemotes || !bs.isUnmodifiedRemote(ltr))) {
				bsList.add(bs);
			}
		}
		return bsList;
	}

	public void setAllDirty() {
		for (PvpBackingStore bs : getEnabledBackingStoresWithUnmodifiedRemotes()) {
			bs.setDirty(true);
		}
	}

	private PvpBackingStore getBackingStoreFile() {
		if (backingStoreFile == null) {
			backingStoreFile = new PvpBackingStoreFile(context);
		}
		return backingStoreFile;
	}

	private void loadInternal(LTRunner ltr, PvpDataInterface dataInterface, List<PvpBackingStore> enabledBs) throws UserAskToChangeFileException, PvpException {
		context.ui.notifyInfo("PvpPersistenceInterface.load :: START");

		for (PvpBackingStore bs : enabledBs) {
			bs.clearTransientData();
		}

		// sort with newest first. When the merge is done, it will know that the newest data is loaded, and it it merging in older data
		PvpBackingStore[] sortedBSArr = new PvpBackingStore[enabledBs.size()];
		sortedBSArr = enabledBs.toArray(sortedBSArr);
		Arrays.sort(sortedBSArr, (bs1, bs2) -> Long.compare(bs2.getLastUpdatedDate(ltr), bs1.getLastUpdatedDate(ltr)));

		boolean doMerge = false;
		boolean wasChanged = false; // TODO way to write out if it was changed?
		for (PvpBackingStore bs : sortedBSArr) {
			ltr.nextStep("Loading data from: " + bs.getShortName());
			context.ui.notifyInfo("loading bs : " + bs.getClass().getName());
			context.ui.notifyInfo("update date: " + new Date(bs.getLastUpdatedDate(ltr)));
			final PvpInStreamer fileReader = new PvpInStreamer(bs, context);

			BufferedInputStream inStream = null;
			try {
				inStream = fileReader.getStream(ltr);
			} catch (UserAskToChangeFileException ucf) {
				throw ucf; // this is not a real exception, just a signal that we should go back to configuration options
			} catch (InvalidKeyException e) {
				context.ui.notifyInfo("at InvalidKeyException");
				bs.setException(new PvpException(PvpException.GeneralErrCode.InvalidKey, e));
				continue;
			} catch (PvpException pvpe) {
				bs.setException(pvpe);
				continue;
			} catch (Exception e) {
				context.ui.notifyWarning("PI load Exception: " + e); // TODO might delete this since the exceptin is hadnled by bs
				bs.setException(new PvpException(PvpException.GeneralErrCode.CantOpenDataFile, e).setAdditionalDescription(bs.getDisplayableResourceLocation(ltr)));
				continue;
			}

			// TODO does fileReader.close(); need to be called if we dont get here?

			try {
				PvpDataInterface newDataInterface = DatabaseReader.read(context, inStream);
				if (doMerge) {
					if (dataInterface.mergeData(newDataInterface) != PvpDataMerger.MergeResultState.NO_CHANGE) {
						wasChanged = true;
					}
				} else {
					dataInterface.setData(newDataInterface);
					doMerge = true;
				}
				context.prefs.setEncryptionStrengthBits(fileReader.getAesBits());
			} catch (UserAskToChangeFileException ucf) {
				throw ucf;
			} catch (Exception e) {
				context.ui.notifyWarning("at 37 Exception: ", e);
				bs.setException(new PvpException(PvpException.GeneralErrCode.CantParseXml, e).setOptionalAction(new ExportXmlFile(context, bs)).setAdditionalDescription(bs.getDisplayableResourceLocation(ltr)));
			} finally {
				// note that close is called 2 times when an error happens
				fileReader.close();
			}
		}

		if (!doMerge) { // if this is false, nothing was loaded -> we have a problem
			throw enabledBs.get(0).getException();
		}

		if (wasChanged) {
			// set them as dirty, so they will eventually save
			setAllDirty();
			context.ui.notifyInfo("PvpPersistenceInterface.load :: was changed is TRUE");
		} else {
			boolean wasErrA = false;
			for (PvpBackingStore bs : sortedBSArr) {
				if (bs.getException() != null) {
					wasErrA = true;
				}
			}
			context.ui.notifyInfo("PvpPersistenceInterface.load :: ready to call allStoresAreUpToDate. wasErrA: " + wasErrA);
			if (!wasErrA) {
				for (PvpBackingStore bs : getEnabledBackingStores(ltr, true)) {
					bs.allStoresAreUpToDate(ltr);
				}
			}
		}
	}

	public void load(LTRunner ltr, PvpDataInterface dataInterface) throws UserAskToChangeFileException, PvpException {
		// TODO cleanup
		List<PvpBackingStore> enabledBs = getEnabledBackingStores(ltr, false);
		try {
			for (PvpBackingStore bs : enabledBs) {
				bs.stateTrans(PvpBackingStore.BsStateTrans.StartLoading);
			}
			loadInternal(ltr, dataInterface, enabledBs);
		} finally {
			for (PvpBackingStore bs : enabledBs) {
				bs.stateTrans(PvpBackingStore.BsStateTrans.EndLoading);
			}
		}
	}

	/**
	 * return true to Quit. Return false to cancel Quit, and keep app running
	 */
	public boolean appQuiting(LTRunner ltr) {
		save(ltr, context.data.getDataInterface(), SaveTrigger.quit);
		return !errorHappened;
	}

	private void saveInternal(LTRunner ltr, PvpDataInterface dataInterface, SaveTrigger saveTrig, List<PvpBackingStore> enabledBs) {
		errorHappened = false;

		for (PvpBackingStore bs : enabledBs) {
			PvpBackingStoreLTCB pvpBsLtCb = new PvpBackingStoreLTCB(bs);
			try {
				pvpBsLtCb.taskStarting(null);
				switch (bs.getChattyLevel()) {
					case unlimited:
					case localLevel:
					case remoteHeavy:
						if (saveTrig == SaveTrigger.quit) {
							if (bs.isDirty()) {
								saveOneBackingStore(ltr, dataInterface, bs);
							}
						} else {
							saveOneBackingStore(ltr, dataInterface, bs);
						}
						break;
					case remoteMedium:
					case remoteLight:
					case mostRestricted:
						if (saveTrig == SaveTrigger.quit) {
							if (bs.isDirty()) {
								saveOneBackingStore(ltr, dataInterface, bs);
							}
						} else if (saveTrig == SaveTrigger.major) {
							saveOneBackingStore(ltr, dataInterface, bs);
						} else {
							bs.setDirty(true);
						}
						break;
				}
			} finally {
				pvpBsLtCb.taskComplete(null);
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
				bs.allStoresAreUpToDate(ltr);
			}
		}
	}

	public void save(LTRunner ltr, PvpDataInterface dataInterface, SaveTrigger saveTrig) {
		final List<PvpBackingStore> enabledBs = getEnabledBackingStores(ltr, true);
		List<PvpBackingStore> cantSaveNowBs = null;

		PvpBackingStore.BsStateTrans stateTrans;
		if (saveTrig == SaveTrigger.init) {
			saveTrig = SaveTrigger.major;
			stateTrans = PvpBackingStore.BsStateTrans.initSave;
		} else {
			stateTrans = PvpBackingStore.BsStateTrans.StartSaving;
		}
		for (PvpBackingStore bs : enabledBs) {
			try {
				bs.stateTrans(stateTrans);
			} catch (Exception e) {
				if (cantSaveNowBs == null) {
					cantSaveNowBs = new ArrayList<>();
				}
				this.context.ui.notifyWarning("Cant save BS now: " + bs.getClass().getName());
				cantSaveNowBs.add(bs);
			}
		}
		if (cantSaveNowBs != null) {
			enabledBs.removeAll(cantSaveNowBs);
			if (enabledBs.size() == 0) {
				PvpContextUI.getActiveUI().showMessageDialog("In Progress", "Saving is already in progress. Please try again a bit later.");
				throw new RuntimeException("No Backing Stores can be saved right now");
			}
		}

		try {
			saveInternal(ltr, dataInterface, saveTrig, enabledBs);
		} finally {
			for (PvpBackingStore bs : enabledBs) {
				bs.stateTrans(PvpBackingStore.BsStateTrans.EndSaving);
			}
		}
	}

	private void saveOneBackingStore(LTRunner ltr, PvpDataInterface dataInterface, PvpBackingStore bs) {
		context.ui.notifyInfo("PvpPersistenceInterface.saveOneBackingStore.START:" + bs.getClass().getName());

		ltr.nextStep("Saving data to: " + bs.getShortName());
		bs.clearTransientData();
		try {
			if (bs.supportsFileUpload()) {
				context.ui.notifyInfo("PvpPersistenceInterface.saveOneBackingStore.FILE_UPLOAD");
				bs.clearTransientData();
				if (!bs.isUnmodifiedRemote(ltr)) {
					ltr.nextStep("Loading new changes from remote");
					context.ui.notifyInfo("PvpPersistenceInterface.saveOneBackingStore.load :: remote file has updated - reloading...");
					loadInternal(ltr, dataInterface, this.getEnabledBackingStores(ltr, false));
				}
				if (getBackingStoreFile().isDirty()) {
					ltr.nextStep("Saving local file");
					context.ui.notifyInfo("PvpPersistenceInterface.saveOneBackingStore.save :: saving local file");
					saveOneBackingStore(ltr, dataInterface, getBackingStoreFile());
				}
				ltr.nextStep("Uploading file to remote");
				bs.doFileUpload(ltr);
			} else {
				PvpOutStreamer fileWriter = null;
				try {
					fileWriter = new PvpOutStreamer(ltr, bs, context.prefs);
					DatabaseWriter.write(context, fileWriter.getWriter(ltr), dataInterface);
				} finally {
					// note that close is called 2 times if Exception thrown from fileWriter.getWriter()
					if (fileWriter != null) {
						fileWriter.close();
					}
				}
			}
			bs.setDirty(false);
		} catch (PvpException e) {
			bs.setException(e);
			if (!e.isRecoverable()) {
				errorHappened = true;
				context.ui.notifyBadException(e, true, PvpException.GeneralErrCode.CantWriteDataFile);
			}
		} catch (Exception e) {
			errorHappened = true;
			bs.setException(new PvpException(PvpException.GeneralErrCode.CantWriteDataFile, e));
			context.ui.notifyBadException(e, true, PvpException.GeneralErrCode.CantWriteDataFile);
		}
		context.ui.notifyInfo("PvpPersistenceInterface.saveOneBackingStore.END");
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
			bs.stateTrans(PvpBackingStore.BsStateTrans.StartSaving);
		}
		public void runLongTask(LTRunner ltr) {
			try {
				saveOneBackingStore(ltr, dataInterface, bs);
			} finally {
				bs.stateTrans(PvpBackingStore.BsStateTrans.EndSaving);
			}
		}
	}

	public LongTask loadcheckOneBackingStoreLT(PvpContext altContextParam, PvpBackingStore bs, boolean showUI) {
		return new LongTaskLoadcheckOneBS(altContextParam, bs, showUI);
	}

	class LongTaskLoadcheckOneBS implements LongTask {
		private PvpContext altContext;
		private PvpBackingStore bs;
		final boolean showUI;
		public LongTaskLoadcheckOneBS(PvpContext altContextParam, PvpBackingStore bsParam, boolean showUiParam) {
			altContext = altContextParam;
			bs = bsParam;
			showUI = showUiParam;
			bs.stateTrans(PvpBackingStore.BsStateTrans.StartLoading);
		}
		public void runLongTask(LTRunner ltr) throws Exception {
			PvpInStreamer fileReader = null;
			try {
				fileReader = new PvpInStreamer(bs, altContext);
				final BufferedInputStream inStream = fileReader.getStream(ltr);
				PvpDataInterface newDataInterface = DatabaseReader.read(altContext, inStream);
				if (altContext.data.getDataInterface().mergeData(newDataInterface) != PvpDataMerger.MergeResultState.NO_CHANGE) {
					if (showUI) {
						altContext.ui.showMessageDialog("Loaded Data", "There were changes that have been loaded");
					}
					List<PvpBackingStore> bsList = getEnabledBackingStores(ltr, true);
					for (PvpBackingStore bsi : bsList) {
						if (bsi != bs) {
							altContext.ui.notifyInfo("LongTaskLoadcheckOneBS :: setting dirty :: " + bsi.getClass().getName());
							bsi.setDirty(true);
						}
					}
					if (altContext.uiMain != null) {
						SwingUtilities.invokeLater(() -> altContext.uiMain.getViewListContext().filterUIChanged());
					}
				} else {
					if (showUI) {
						altContext.ui.showMessageDialog("No Changes", "There were no changes. No new data was loaded.");
					}
				}
			} catch (Exception e) {
				if (showUI) {
					altContext.ui.notifyWarning("Error in LongTaskLoadcheckOneBS", e);
					altContext.ui.showMessageDialog("Error", "There was an error. " + e.getMessage());
				} else {
					throw e;
				}
			} finally {
				bs.stateTrans(PvpBackingStore.BsStateTrans.EndLoading);
				if (fileReader != null) {
					fileReader.close();
				}
			}
		}
	}

}
