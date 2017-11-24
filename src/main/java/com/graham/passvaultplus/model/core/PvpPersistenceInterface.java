/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.BufferedInputStream;
import java.security.InvalidKeyException;
import java.util.ArrayList;
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

	public void load(PvpDataInterface dataInterface) throws UserAskToChangeFileException, PvpException {
		final PvpInStreamer fileReader = new PvpInStreamer(backingStores.get(0), context);
		
		BufferedInputStream inStream = null;
		try {
			inStream = fileReader.getStream();
		} catch (UserAskToChangeFileException ucf) {
			throw ucf; // this is not a real exception, just a signal that we should go back to configuration options
		} catch (InvalidKeyException e) {
			throw new PvpException(PvpException.GeneralErrCode.InvalidKey, e);
		} catch (Exception e) {
			throw new PvpException(PvpException.GeneralErrCode.CantOpenDataFile, e).setAdditionalDescription("File: " + context.getDataFile());
		}
		
		try {
			PvpDataInterface newDataInterface = DatabaseReader.read(context, inStream);
			dataInterface.setData(newDataInterface);
			context.setEncryptionStrengthBits(fileReader.getAesBits());
		} catch (UserAskToChangeFileException ucf) {
			throw ucf;
		} catch (Exception e) {
			throw new PvpException(PvpException.GeneralErrCode.CantParseXml, e).setOptionalAction(new ExportXmlFile(context)).setAdditionalDescription("File: " + context.getDataFile());
		} finally {
			// note that close is called 2 times when an error happens
			fileReader.close();
		}
	}
	
	/**
	 * return true to Quit. Return false to cancel Quit, and keep app running
	 */
	public boolean appQuiting() {
		save(context.getDataInterface(), SaveTrigger.quit);
		return true;
	}
	
	public void save(PvpDataInterface dataInterface, SaveTrigger saveTrig) {
		for (PvpBackingStore bs : backingStores) {
			if (bs.isEnabled()) {
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
		if (bs.supportsFileUpload()) {
			bs.doFileUpload();
		} else {
			PvpOutStreamer fileWriter = null;
			try {
				fileWriter = new PvpOutStreamer(bs, context);
				DatabaseWriter.write(context, fileWriter.getWriter(), dataInterface);
			} catch (Exception e) {
				context.notifyBadException(e, true, PvpException.GeneralErrCode.CantWriteDataFile);
			} finally {
				// note that close is called 2 times if Exception thrown from fileWriter.getWriter()
				if (fileWriter != null) {
					fileWriter.close();
				}
			}
		}
		bs.setDirty(false);
	}

}
