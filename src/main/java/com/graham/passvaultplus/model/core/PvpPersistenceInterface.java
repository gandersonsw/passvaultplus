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
	public static final String EXT_COMPRESS = "zip";
	public static final String EXT_ENCRYPT = "bmn";
	public static final String EXT_XML = "xml";
	
	final private PvpContext context;

	public PvpPersistenceInterface(final PvpContext contextParam) {
		context = contextParam;
	}
	
	private List<PvpBackingStore> getBackingStores() {
		List<PvpBackingStore> bs = new ArrayList<PvpBackingStore>();
		bs.add(new PvpBackingStoreFile(context.getDataFile()));
		return bs;
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
		final List<PvpBackingStore> bs = getBackingStores();
		final PvpInStreamer fileReader = new PvpInStreamer(bs.get(0), context);
		
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
	
	public void save(PvpDataInterface dataInterface) {
		final List<PvpBackingStore> bs = getBackingStores();
		PvpOutStreamer fileWriter = null;
		try {
			fileWriter = new PvpOutStreamer(bs.get(0), context);
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

}
