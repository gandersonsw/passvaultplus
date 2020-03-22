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
import com.graham.passvaultplus.PvpContextUI;
import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.UserAskToChangeFileException;
import com.graham.passvaultplus.view.longtask.LTRunner;

/**
 * Get a stream for the database file. Take care of encryption and compression.
 */
public class PvpInStreamer {
	
	private final PvpContext context;
	private final PvpBackingStore backingStore;
	
	private BufferedInputStream bufInStream = null;
	private InputStream inStream = null;
	private ZipInputStream zipfile = null;
	private CipherInputStream cipherStream = null;
	private InputStream bsInputStream = null;
	private EncryptionHeader eHeader;
	
	public PvpInStreamer(final PvpBackingStore bs, final PvpContext contextParam) {
		backingStore = bs;
		context = contextParam;
	}
	
	public BufferedInputStream getStream(LTRunner ltr) throws IOException, PvpException, UserAskToChangeFileException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		try {
			// 4 possible file extensions: .xml .zip .bmn.zip .bmn
			if (backingStore.isEncrypted(ltr, true)) {
				openCipherInputStream(ltr);
			} else {
				bsInputStream = backingStore.openInputStream(ltr);
				inStream = bsInputStream;
			}
			
			if (backingStore.isCompressed(ltr, true)) {
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

	public boolean validatePassword(LTRunner ltr, String password)
			throws IOException, PvpException, InvalidKeySpecException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
		try {
			bsInputStream = backingStore.openInputStream(ltr);
			eHeader = getEncryptHeader(bsInputStream);
			final Cipher cer = MyCipherFactory.createCipher(password, eHeader, Cipher.DECRYPT_MODE);

			cipherStream = new CipherInputStream(bsInputStream, cer);
			inStream = cipherStream;

			byte[] check = new byte[8];
			if (inStream.read(check, 0, 8) == 8) {
				final String s = new String(check);
				if (s.equals("remthis7")) {
					// this is how we know the password worked.
					return true;
				}
			} else {
				PvpContextUI.getActiveUI().notifyWarning("PvpInStreamer.openCipherInputStream: could not read 8 bytes: " + backingStore.getShortName());
			}
		} finally {
			closeCipherAndFile();
		}
		return false;
	}
	
	private void openCipherInputStream(LTRunner ltr) throws IOException, PvpException, UserAskToChangeFileException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		boolean passwordIsGood = false;
		boolean passwordTried = false;
		while (!passwordIsGood) {
			try {
				bsInputStream = backingStore.openInputStream(ltr);
				
				final String password = context.prefs.getPasswordOrAskUser(passwordTried, backingStore.getDisplayableResourceLocation(ltr));
				eHeader = getEncryptHeader(bsInputStream);
				final Cipher cer = MyCipherFactory.createCipher(password, eHeader, Cipher.DECRYPT_MODE);
	
				cipherStream = new CipherInputStream(bsInputStream, cer);
				inStream = cipherStream;
	
				byte[] check = new byte[8];
				if (inStream.read(check, 0, 8) == 8) {
					final String s = new String(check);
					if (s.equals("remthis7")) {
						// this is how we know the password worked.
						passwordIsGood = true;
					}
				} else {
					PvpContextUI.getActiveUI().notifyWarning("PvpInStreamer.openCipherInputStream: could not read 8 bytes: " + backingStore.getShortName());
				}
				if (!passwordIsGood && backingStore.getChattyLevel().isRemote()) {
					throw new PvpException(PvpException.SpecificErrCode.RemotePasswordBad, null);
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
	
	private static EncryptionHeader getEncryptHeader(final InputStream iStream ) throws IOException, PvpException {
		int ehs = EncryptionHeader.getEncryptHeaderSize();
		int totalBytesRead = 0;
		final byte[] encryptionHeaderBytes = new byte[EncryptionHeader.getEncryptHeaderSize()];
		while (totalBytesRead < ehs) {
			// This while loop is here because in GoogleDocs API, sometimes it will not return the full data on the first call
			final int br = iStream.read(encryptionHeaderBytes, totalBytesRead, EncryptionHeader.getEncryptHeaderSize() - totalBytesRead);
			if (br == -1) {
				break;
			}
			totalBytesRead += br;
		}
		if (totalBytesRead != EncryptionHeader.getEncryptHeaderSize()) {
			final String msg = "bytes read: " + totalBytesRead + " bytes expected: " + EncryptionHeader.getEncryptHeaderSize();
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
		if (bsInputStream != null) {
			try {
				bsInputStream.close();
			} catch (IOException ioe) {
			}
			bsInputStream = null;
		}
	}
}
