/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class MyCipherFactory {
	
	/**
	 * encrypt mode
	 */
	public static Cipher createCipher(final String password, final EncryptionHeader header, int mode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
        final IvParameterSpec ivps = new IvParameterSpec(header.ivseed);
        
        System.out.println("MyCipherFactory: header.aesStrengthBits=" + header.aesStrengthBits);
        final PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), header.salt, header.iterationCount, header.aesStrengthBits);
        final SecretKeySpec aesKey = generateAESKey(spec);
        
	    final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    cipher.init(mode, aesKey, ivps);
	    return cipher;
	}
	
	private static SecretKeySpec generateAESKey(final PBEKeySpec spec) throws NoSuchAlgorithmException, InvalidKeySpecException {
		final SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		final byte encodedPBEKey[] = skf.generateSecret(spec).getEncoded();
		return new SecretKeySpec(encodedPBEKey, "AES");
	}
	
	static public int getMaxAllowedAESKeyLength() throws NoSuchAlgorithmException {
		return Cipher.getMaxAllowedKeyLength("AES");
	}
}
