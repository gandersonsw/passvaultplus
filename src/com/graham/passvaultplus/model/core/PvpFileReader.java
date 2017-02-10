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

import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;

import com.graham.passvaultplus.PvpContext;

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
	private CipherInputStream cypherStream = null;
	private FileInputStream fileStream = null;

	public PvpFileReader(final File f, final PvpContext contextParam) {
		fileToRead = f;
		fileName = f.getName();
		context = contextParam;
	}
	
	public BufferedInputStream getStream() throws Exception {
		boolean badEncryption = true;
		boolean passwordTried = false;
		try {
			while (badEncryption) {
				// 4 possible file extensions: .xml .zip .bmn.zip .bmn
				if (PvpFileInterface.isEncrypted(fileName)) {
					FileInputStream fis = null;
					try {
						fis = new FileInputStream(fileToRead);
						byte[] encryptionHeaderBytes = new byte[CipherWrapper.getEncryptHeaderSize()];
						int br = fis.read(encryptionHeaderBytes);
						if (br != CipherWrapper.getEncryptHeaderSize()) {
							throw new IOException("encrypt header not read:" + br); // TODO test this exception
						}
						final String password = context.getPasswordOrAskUser(passwordTried);
						CipherWrapper cw = new CipherWrapper(password, encryptionHeaderBytes);

						cypherStream = new CipherInputStream(fis, cw.cipher);
						inStream = cypherStream;

						byte[] check = new byte[8];
						if (inStream.read(check, 0, 8) == 8) {
							String s = new String(check);
							if (s.equals("remthis7")) {
								badEncryption = false; // TODO test this, message to user
							}
						}
						passwordTried = true;

					} catch (NoSuchAlgorithmException e) {
						throw new RuntimeException(e);
					} catch (NoSuchPaddingException e) {
						throw new RuntimeException(e);
					} catch (InvalidKeyException e) {
						// TODO handle better, test
						context.notifyBadException("Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy", e, true);
						throw new RuntimeException(e);
					} catch (InvalidKeySpecException e) {
						throw new RuntimeException(e);
					} catch (InvalidAlgorithmParameterException e) {
						throw new RuntimeException(e);
					} finally {
						// if cypherStream is null, some kind of error happened,
						// so close the file stream
						if (cypherStream == null && fis != null) {
							try {
								fis.close();
							} catch (Exception e) {
							}
						}
					}

				} else {
					badEncryption = false;
					fileStream = new FileInputStream(fileToRead);
					inStream = fileStream;
				}

				if (!badEncryption) {
					if (PvpFileInterface.isCompressed(fileName)) {
						zipfile = new ZipInputStream(inStream);
						ZipEntry entry = zipfile.getNextEntry();
						if (entry != null) {
							inStream = zipfile;
						}
					}
				}
			}
			
			bufInStream = new BufferedInputStream(inStream);
		} catch (Exception e) {
			// if an error happened, close anything that may be opened, as we are only partially set-up
			close();
			throw e;
		}
		
		return bufInStream;
	}
	
	public void close() {
		if (bufInStream != null) {
			try {
				bufInStream.close();
			} catch (IOException ioe) {
			}
			bufInStream = null;
		}
		if (cypherStream != null) {
			try {
				cypherStream.close();
			} catch (IOException ioe) {
			}
			cypherStream = null;
		}
		if (fileStream != null) {
			try {
				fileStream.close();
			} catch (IOException ioe) {
			}
			fileStream = null;
		}
		// bufInStream.close() will also close inStream

		if (zipfile != null) {
			try {
				zipfile.close();
			} catch (IOException ioe) {
			}
			zipfile = null;
		}
	}
}
