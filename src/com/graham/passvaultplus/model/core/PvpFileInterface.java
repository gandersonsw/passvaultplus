/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.BufferedInputStream;
import java.security.InvalidKeyException;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.UserAskToChangeFileException;
import com.graham.passvaultplus.actions.ExportXmlFile;

/**
 * All methods to load data into RtDataInterface from the file
 */
public class PvpFileInterface {
	public static final String EXT_COMPRESS = "zip";
	public static final String EXT_ENCRYPT = "bmn";
	public static final String EXT_XML = "xml";

	final private PvpContext context;

	public PvpFileInterface(final PvpContext contextParam) {
		context = contextParam;
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
			fname = fname + "." + PvpFileInterface.EXT_COMPRESS;
		}
		if (encrypted) {
			fname = fname + "." +  PvpFileInterface.EXT_ENCRYPT;
		}
		if (!compressed && !encrypted) {
			fname = fname + "." + PvpFileInterface.EXT_XML;
		}
		return fname;
	}

	public void load(PvpDataInterface dataInterface) throws UserAskToChangeFileException, PvpException {
		final PvpFileReader fileReader = new PvpFileReader(context.getDataFile(), context);
		
		BufferedInputStream inStream = null;
		try {
			inStream = fileReader.getStream();
		} catch (UserAskToChangeFileException ucf) {
			throw ucf; // this is not a real exception, just a signal that we should go back to configuration options
		} catch (InvalidKeyException e) {
			//context.notifyBadException("Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy", e, false, PvpException.GeneralErrCode.InvalidKey);
			throw new PvpException(PvpException.GeneralErrCode.InvalidKey, e);
		} catch (Exception e) {
			//context.notifyBadException("cannot open data file", e, false, PvpException.GeneralErrCode.CantOpenDataFile);
			throw new PvpException(PvpException.GeneralErrCode.CantOpenDataFile, e).setAdditionalDescription("File: " + context.getDataFile());
		}
		
		try {
			PvpDataInterface newDataInterface = DatabaseReader.read(context, inStream);
			dataInterface.setData(newDataInterface);
			context.setEncryptionStrengthBits(fileReader.getAesBits());
		} catch (UserAskToChangeFileException ucf) {
			throw ucf;
		} catch (Exception e) {
			// todo test this and improve handling
			//context.notifyBadException("cannot parse xml", e, false, new ExportXmlFile(context), PvpException.GeneralErrCode.CantParseXml);
			throw new PvpException(PvpException.GeneralErrCode.CantParseXml, e).setOptionalAction(new ExportXmlFile(context)).setAdditionalDescription("File: " + context.getDataFile());
		} finally {
			// TODO note that close is called 2 times if Exception thrown from fileReader.getStream(). Fix this?
			fileReader.close();
		}
	}
	
	public void save(PvpDataInterface dataInterface) {
		PvpFileWriter fileWriter = new PvpFileWriter(context.getDataFile(), context);
		try {
			DatabaseWriter.write(context, fileWriter.getWriter(), dataInterface);
		} catch (Exception e) {
			// TODO test this and improve handling
			context.notifyBadException(e, true, PvpException.GeneralErrCode.CantWriteDataFile);
			// TODO should we throw this exception?
			//throw new PvpException(PvpException.GeneralErrCode.CantWriteDataFile, e);
		} finally {
			// TODO note that close is called 2 times if Exception thrown from fileWriter.getWriter(). Fix this?
			fileWriter.close();
		}
	}

}
