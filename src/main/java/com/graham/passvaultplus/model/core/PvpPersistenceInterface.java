/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.BufferedInputStream;
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

/**
 * All methods to load data into RtDataInterface from the file
 */
public class PvpPersistenceInterface {
	public enum SaveTrigger {
		quit, // the application it quiting
		cud,  // there was a Create, Update or Delete
		major // some major change
	}
	
	public static final String EXT_COMPRESS = "zip";
	public static final String EXT_ENCRYPT = "bmn";
	public static final String EXT_XML = "xml";
	
	final private PvpContext context;
	final private List<PvpBackingStore> backingStores;
	private boolean errorHappened;

	public PvpPersistenceInterface(final PvpContext contextParam) {
		context = contextParam;
		backingStores = new ArrayList<PvpBackingStore>();
		backingStores.add(new PvpBackingStoreFile(context)); // File needs to be the first Backing Store in the list
		backingStores.add(new PvpBackingStoreGoogleDocs(context));
	}

	public static boolean isCompressed(final String path) {
		// path is .zip or .zip.bmn
		return path.endsWith("." + EXT_COMPRESS) || path.endsWith("." + EXT_COMPRESS + "." + EXT_ENCRYPT);
	}

	public static boolean isEncrypted(final String path) {
		// path is .bmn or .zip.bmn
		return path.endsWith("." + EXT_ENCRYPT);
	}
	
	public static String formatFileName(final String fnameWithExt, final boolean compressed, final boolean encrypted) {
		String fname = BCUtil.getFileNameNoExt(fnameWithExt, true);
		if (compressed) {
			fname = fname + "." + PvpPersistenceInterface.EXT_COMPRESS;
		}
		if (encrypted) {
			fname = fname + "." +  PvpPersistenceInterface.EXT_ENCRYPT;
		}
		if (!compressed && !encrypted) {
			fname = fname + "." + PvpPersistenceInterface.EXT_XML;
		}
		return fname;
	}
	
	public List<PvpBackingStore> getBackingStores() {
		return backingStores;
	}

	public void load(PvpDataInterface dataInterface) throws UserAskToChangeFileException, PvpException {
		
		for (PvpBackingStore bs: backingStores) {
			bs.clearTransientData();
		}
		
		// sort with newest first. When the merge is done, it will know that the newest data is loaded, and it it merging in older data
		PvpBackingStore[] sortedBSArr = new PvpBackingStore[backingStores.size()];
		sortedBSArr = backingStores.toArray(sortedBSArr);
		Arrays.sort(sortedBSArr, (bs1, bs2) -> { return Long.compare(bs2.getLastUpdatedDate(), bs1.getLastUpdatedDate()); } );
		
		boolean doMerge = false;
		boolean wasChanged = false; // TODO way to write out if it was changed?
		for (PvpBackingStore bs: sortedBSArr) {
			if (bs.isEnabled()) {
				System.out.println("loading bs : " + bs.getClass().getName());
				System.out.println("update date: " + new Date(bs.getLastUpdatedDate()));
				final PvpInStreamer fileReader = new PvpInStreamer(bs, context);
				
				BufferedInputStream inStream = null;
				try {
					inStream = fileReader.getStream();
				} catch (UserAskToChangeFileException ucf) {
					throw ucf; // this is not a real exception, just a signal that we should go back to configuration options
				} catch (InvalidKeyException e) {
					System.out.println("at InvalidKeyException");
					bs.setException(new PvpException(PvpException.GeneralErrCode.InvalidKey, e));
					continue;
					//throw new PvpException(PvpException.GeneralErrCode.InvalidKey, e);
				} catch (Exception e) {
					System.out.println("at Exception: " + e.getMessage());
					bs.setException(new PvpException(PvpException.GeneralErrCode.CantOpenDataFile, e).setAdditionalDescription(bs.getDisplayableResourceLocation()));
					continue;
					//throw new PvpException(PvpException.GeneralErrCode.CantOpenDataFile, e).setAdditionalDescription(getFileDesc(bs));
				} 
					
				// TODO does fileReader.close(); need to be called if we dont get here?
				
				try {
					PvpDataInterface newDataInterface = DatabaseReader.read(context, inStream);
					if (doMerge) {
						if (dataInterface.mergeData(newDataInterface)) {
							wasChanged = true;
						}
					} else {
						dataInterface.setData(newDataInterface);
						doMerge = true;
					}
					context.setEncryptionStrengthBits(fileReader.getAesBits());
				} catch (UserAskToChangeFileException ucf) {
					throw ucf;
				} catch (Exception e) {
					bs.setException(new PvpException(PvpException.GeneralErrCode.CantParseXml, e).setOptionalAction(new ExportXmlFile(context, bs)).setAdditionalDescription(bs.getDisplayableResourceLocation()));
				} finally {
					// note that close is called 2 times when an error happens
					fileReader.close();
				}
			}
		}
		
		if (!doMerge) { // if this is false, nothing was loaded -> we have a problem
			throw backingStores.get(0).getException();
		}
		
		if (wasChanged) {
			// set them as dirty, so they will eventually save
			for (PvpBackingStore bs: backingStores) {
				bs.setDirty(true);
			}
			
			System.out.println("was changed is TRUE");
		}
	}
	
	/**
	 * return true to Quit. Return false to cancel Quit, and keep app running
	 */
	public boolean appQuiting() {
		save(context.getDataInterface(), SaveTrigger.quit);
		return !errorHappened;
	}
	
	public void save(PvpDataInterface dataInterface, SaveTrigger saveTrig) {
		errorHappened = false;
		for (PvpBackingStore bs : backingStores) {
			if (bs.isEnabled()) {
				bs.clearTransientData();
				switch (bs.getChattyLevel()) {
					case unlimited:
					case localLevel:
					case remoteHeavy:
						if (saveTrig == SaveTrigger.quit) {
							if (bs.isDirty()) {
								saveOneBackingStore(dataInterface, bs);
							}
						} else {
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
						} else if (saveTrig == SaveTrigger.major) {
							saveOneBackingStore(dataInterface, bs);
						} else {
							bs.setDirty(true);
						}
						break;
				}
			}
		}
	}
	
	private void saveOneBackingStore(PvpDataInterface dataInterface, PvpBackingStore bs) {
		System.out.println("SAVING BACKING STORE:" + bs.getClass().getName());
		try {
			if (bs.supportsFileUpload()) {
				bs.doFileUpload();
			} else {
				PvpOutStreamer fileWriter = null;
				try {
					fileWriter = new PvpOutStreamer(bs, context);
					DatabaseWriter.write(context, fileWriter.getWriter(), dataInterface);
				} finally {
					// note that close is called 2 times if Exception thrown from fileWriter.getWriter()
					if (fileWriter != null) {
						fileWriter.close();
					}
				}
			}
			bs.setDirty(false);
		} catch (Exception e) {
			errorHappened = true;
			context.notifyBadException(e, true, PvpException.GeneralErrCode.CantWriteDataFile);
		}
	}

}
