/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.UserAskToChangeFileException;

/**
 * Get a stream for the database file. Take care of encryption and compression.
 */
public class PvpFileReader {
	
	private final File fileToRead;
	private final String fileName;
	private final PvpContext context;
	
	private BufferedInputStream bufInStream = null;
	private InputStream inStream = null;
	private ZipInputStream zipfile = null;
	private CipherInputStream cipherStream = null;
	private FileInputStream fileStream = null;
	private EncryptionHeader eHeader;

	public PvpFileReader(final File f, final PvpContext contextParam) {
		fileToRead = f;
		fileName = f.getName();
		context = contextParam;
	}
	
	public BufferedInputStream getStream() throws IOException, PvpException, UserAskToChangeFileException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		try {
			// 4 possible file extensions: .xml .zip .bmn.zip .bmn
			if (PvpFileInterface.isEncrypted(fileName)) {
				openCipherInputStream();
			} else {
				fileStream = new FileInputStream(fileToRead);
				inStream = fileStream;
			}
			
			if (PvpFileInterface.isCompressed(fileName)) {
				zipfile = new ZipInputStream(inStream);
				ZipEntry entry = zipfile.getNextEntry();
				if (entry != null) {
					inStream = zipfile;
				} else {
					throw new PvpException(PvpException.SpecificErrCode.ZipEntryNotFound, null);
				}
			}
			
			bufInStream = new BufferedInputStream(inStream);
		} finally {
			if (bufInStream == null) {
				// if an error happened, close anything that may be opened, as we are only partially set-up
				close();
			}
		}
		
		return bufInStream;
	}
	
	public int getAesBits() {
		if (eHeader == null) {
			return 0;
		}
		return eHeader.aesStrengthBits;
	}
	
	private void openCipherInputStream() throws IOException, PvpException, UserAskToChangeFileException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		boolean passwordIsGood = false;
		boolean passwordTried = false;
		while (!passwordIsGood) {
			try {
				fileStream = new FileInputStream(fileToRead);
				
				final String password = context.getPasswordOrAskUser(passwordTried);
				eHeader = getEncryptHeader(fileStream);
				final Cipher cer = MyCipherFactory.createCipher(password, eHeader, Cipher.DECRYPT_MODE);
	
				cipherStream = new CipherInputStream(fileStream, cer);
				inStream = cipherStream;
	
				byte[] check = new byte[8];
				if (inStream.read(check, 0, 8) == 8) {
					final String s = new String(check);
					if (s.equals("remthis7")) {
						// this is how we know the password worked. this is not an exception
						passwordIsGood = true;
					}
				}
				passwordTried = true;
			} finally {
				if (!passwordIsGood) {
					closeCipherAndFile();
				}
			}
		}
	}
	
	public static EncryptionHeader getEncryptHeader(final File f) throws IOException, PvpException {
		FileInputStream fStream = null;
		try {
			fStream = new FileInputStream(f);
			return getEncryptHeader(fStream);
		} finally {
			if (fStream != null) {
				try {
					fStream.close();
				} catch (IOException ioe) {
				}
			}
		}
	}
	
	private static EncryptionHeader getEncryptHeader(final FileInputStream fStream ) throws IOException, PvpException {
		final byte[] encryptionHeaderBytes = new byte[EncryptionHeader.getEncryptHeaderSize()];
		final int br = fStream.read(encryptionHeaderBytes);
		if (br != EncryptionHeader.getEncryptHeaderSize()) {
			final String msg = "bytes read: " + br + " bytes expected: " + EncryptionHeader.getEncryptHeaderSize();
			throw new PvpException(PvpException.SpecificErrCode.EncryptionHeaderNotRead, msg);
		}
		return new EncryptionHeader(encryptionHeaderBytes);
	}
	
	public void close() {
		if (bufInStream != null) {
			try {
				bufInStream.close();
			} catch (IOException ioe) {
			}
			bufInStream = null;
		}
		
		closeCipherAndFile();

		if (zipfile != null) {
			try {
				zipfile.close();
			} catch (IOException ioe) {
			}
			zipfile = null;
		}
	}
	
	private void closeCipherAndFile() {
		if (cipherStream != null) {
			try {
				cipherStream.close();
			} catch (IOException ioe) {
			}
			cipherStream = null;
		}
		if (fileStream != null) {
			try {
				fileStream.close();
			} catch (IOException ioe) {
			}
			fileStream = null;
		}
	}
}
