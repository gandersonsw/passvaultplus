/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.ByteArrayOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import com.graham.passvaultplus.PvpException;

public class StringEncrypt {
	
	private static final String VERIFY_TEXT = "cmeuyhsb";

	public static byte[] printBytes(final byte[] ba) {
		int total = 0;
		for (int i = 0; i < ba.length; i++) {
			System.out.print(ba[i]);
			System.out.print(",");
			total += Math.abs(ba[i]);
		}
		
		System.out.println(" ");
		System.out.println("total=" + total);
		return ba;
	}
	
	/**
	 * @return null if the pin was not correct
	 */
	public static String decryptString(final byte[] ba, final String pin, final boolean usepin) throws PvpException {
		try {
			final byte[] headerBytes = new byte[EncryptionHeader.getEncryptHeaderSize()];
			for (int i = 0; i < headerBytes.length; i++) {
				headerBytes[i] = ba[i];
			}
			final byte[] dataBytes = new byte[ba.length - headerBytes.length];
			for (int i = headerBytes.length; i < ba.length; i++) {
				dataBytes[i - headerBytes.length] = ba[i];
			}
			final EncryptionHeader eHeader = new EncryptionHeader(headerBytes);
			final Cipher cer = MyCipherFactory.createCipher(usepin ? pin : "46383947", eHeader , Cipher.DECRYPT_MODE);
			final String encs = new String(cer.doFinal(dataBytes));
			if (encs.length() >= VERIFY_TEXT.length()) {
				final String verifyTextFromba = encs.substring(0, VERIFY_TEXT.length());
				if (verifyTextFromba.equals(VERIFY_TEXT)) {
					return encs.substring(VERIFY_TEXT.length());
				}
			}
			return null;
		} catch (BadPaddingException bpe) {
			//bpe.printStackTrace();
			return null;
		} catch (Exception e) {
			throw new PvpException(PvpException.GeneralErrCode.CantDecryptPassword, e);
		}
	}
	
	public static byte[] encryptString(final String s, final String pin, final boolean usepin) throws PvpException {
		try {
			final EncryptionHeader header = new EncryptionHeader(128);
			final Cipher cer = MyCipherFactory.createCipher(usepin ? pin : "46383947", header, Cipher.ENCRYPT_MODE);
			
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bos.write(header.createEncryptionHeaderBytes());
			final String encs = VERIFY_TEXT + s;
			bos.write(cer.doFinal(encs.getBytes()));
			bos.flush();
			return bos.toByteArray();
		} catch (Exception e) {
			throw new PvpException(PvpException.GeneralErrCode.CantEncryptPassword, e);
		}
	}

}
