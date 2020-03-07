/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import com.graham.passvaultplus.PvpContextPrefs;
import com.graham.passvaultplus.UserAskToChangeFileException;
import com.graham.util.FileUtil;

/**
 * Get a Writer for the database file. Take care of encryption and compression.
 */
public class PvpOutStreamer {

	private final PvpBackingStore backingStore;
	private String password;
	private int encryptionStrength;
	private OutputStream bsOutStream;
	private ZipOutputStream zipStream;
	private CipherOutputStream cipherStream;
	private OutputStreamWriter writer;
	private BufferedWriter bufWriter;
	private OutputStream outStream; // do not close this, this is used as the last output

	public PvpOutStreamer(final PvpBackingStore bs, final PvpContextPrefs cp) throws UserAskToChangeFileException {
		backingStore = bs;
		if (backingStore.isEncrypted(false)) {
			password = cp.getPasswordOrAskUser(false, backingStore.getDisplayableResourceLocation());
			encryptionStrength = cp.getEncryptionStrengthBits();
		}
	}

//	public PvpOutStreamer(final PvpBackingStore bs, final String passwordParam, final int encryptionStrengthParam) {
//		backingStore = bs;
//		password = passwordParam;
//		encryptionStrength = encryptionStrengthParam;
//	}

	/**
	 * When writing, encryption happens first, then compression.
	 */
	public BufferedWriter getWriter() throws Exception {
		//DateUtil.checkBackupFileHourly(fileToWrite);

		try {
			if (backingStore.isEncrypted(false)) {
				final EncryptionHeader header = new EncryptionHeader(encryptionStrength);
				Cipher cer = MyCipherFactory.createCipher(password, header, Cipher.ENCRYPT_MODE);
				OutputStream bsos = backingStore.openOutputStream();
				bsos.write(header.createEncryptionHeaderBytes());
				cipherStream = new CipherOutputStream(bsos, cer);
				outStream = cipherStream;

				// write the code so we know it decrypted successfully
				outStream.write("remthis7".getBytes());
			} else {
				bsOutStream = backingStore.openOutputStream();
				outStream = bsOutStream;
			}

			if (backingStore.isCompressed(false)) {
				zipStream = new ZipOutputStream(outStream);
				String zippedFileName = FileUtil.setFileExt("PvpData",
						backingStore.isEncrypted(false) ? PvpPersistenceInterface.EXT_ENCRYPT : PvpPersistenceInterface.EXT_XML, true);
				zipStream.putNextEntry(new ZipEntry(zippedFileName));
				outStream = zipStream;
			}

			writer = new OutputStreamWriter(outStream, "UTF-8");
			bufWriter = new BufferedWriter(writer);
		} catch (Exception e) {
			// if an error happened, close anything that may be opened, as we are only partially set-up
			close();
			throw e;
		}
		return bufWriter;
	}

	public void close() {
		if (bufWriter != null) {
			try {
				bufWriter.write("               ");
				bufWriter.flush();
			} catch (IOException e1) {
			}
			try {
				bufWriter.close();
			} catch (IOException e1) {
			}
			bufWriter = null;
		}
		if (cipherStream != null) {
			try {
				cipherStream.flush();
			} catch (IOException e) {
			}
			try {
				cipherStream.close();
			} catch (IOException e) {
			}
			cipherStream = null;
		}
		if (zipStream != null) {
			try {
				zipStream.closeEntry();
			} catch (IOException e) {
			}
			try {
				zipStream.close();
			} catch (IOException e) {
			}
			zipStream = null;
		}
		if (bsOutStream != null) {
			try {
				bsOutStream.close();
			} catch (IOException e) {
			}
			bsOutStream = null;
		}
	}
}
