/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.AppUtil;
import com.graham.passvaultplus.PvpContext;

/**
 * Get a Writer for the database file. Take care of encryption and compression.
 */
public class PvpFileWriter {
	
	private final File fileToWrite;
	private final String fileName;
	private final PvpContext context;

	private FileOutputStream fileStream;
	private ZipOutputStream zipStream;
	private CipherOutputStream cipherStream;
	private OutputStreamWriter writer;
	private BufferedWriter bufWriter;
	private OutputStream outStream; // do not close this, this is used as the last output

	public PvpFileWriter(final File f, final PvpContext contextParam) {
		fileToWrite = f;
		fileName = f.getName();
		context = contextParam;
	}

	/**
	 * When writing, encryption happens first, then compression.
	 */
	public BufferedWriter getWriter() throws Exception {
		AppUtil.checkBackupFileHourly(fileToWrite);

		try {
			if (PvpFileInterface.isEncrypted(fileName)) {
				final String password = context.getPasswordOrAskUser(false);
				final EncryptionHeader header = new EncryptionHeader(context.getEncryptionStrengthBits());
				Cipher cer = MyCipherFactory.createCipher(password, header, Cipher.ENCRYPT_MODE);
				FileOutputStream fos = new FileOutputStream(fileToWrite);
				fos.write(header.createEncryptionHeaderBytes());
				cipherStream = new CipherOutputStream(fos, cer);
				outStream = cipherStream;
	
				// write the code so we know it decrypted successfully
				outStream.write("remthis7".getBytes());
			} else {
				fileStream = new FileOutputStream(fileToWrite);
				outStream = fileStream;
			}
	
			if (PvpFileInterface.isCompressed(fileName)) {
				zipStream = new ZipOutputStream(outStream);
				String zippedFileName = BCUtil.setFileExt(fileName,
						PvpFileInterface.isEncrypted(fileName) ? PvpFileInterface.EXT_ENCRYPT : PvpFileInterface.EXT_XML, true);
				zipStream.putNextEntry(new ZipEntry(zippedFileName));
				outStream = zipStream;
			}
	
			writer = new OutputStreamWriter(outStream);
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
		if (fileStream != null) {
			try {
				fileStream.close();
			} catch (IOException e) {
			}
			fileStream = null;
		}
	}
}
