/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherWrapper {
	
	final static int SALT_SIZE = 16; // numbers of bytes of salt
	final static int IV_SEED_SIZE = 16; // Initialization Vector seed size
	
	public final Cipher cipher;
	public final int iterationCount;
	public final byte[] salt;
	public final byte[] ivseed;
	public final int aesStrengthBits;
	
	/**
	 * encrypt mode
	 */
	public CipherWrapper(final String password) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		iterationCount = 10000;
		aesStrengthBits = 128;
		
        final SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        salt = new byte[SALT_SIZE];
        sr.nextBytes(salt);
        
        ivseed = sr.generateSeed(IV_SEED_SIZE);
        final IvParameterSpec ivps = new IvParameterSpec(ivseed);
        
        final PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, aesStrengthBits);
        final SecretKeySpec aesKey = generateAESKey(spec);
        
	    cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivps);
	}
	
	/**
	 * decrypt mode
	 */
	public CipherWrapper(final String password, byte[] encryptionHeaderBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException {
		final ByteBuffer bb = ByteBuffer.wrap(encryptionHeaderBytes);
		int version = bb.getInt();
		if (version != 1) {
			throw new RuntimeException("unsupported version number:" + version);
		}
		aesStrengthBits = bb.getInt();
		iterationCount = bb.getInt();
		salt = new byte[SALT_SIZE];
		bb.get(salt, 0, SALT_SIZE);
		ivseed = new byte[IV_SEED_SIZE];
		bb.get(ivseed, 0, IV_SEED_SIZE);
		
		final IvParameterSpec ivps = new IvParameterSpec(ivseed);
		
        final PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, aesStrengthBits);
        final SecretKeySpec aesKey = generateAESKey(spec);
		
	    cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	    cipher.init(Cipher.DECRYPT_MODE, aesKey, ivps);
	}
	
	private static SecretKeySpec generateAESKey(final PBEKeySpec spec) throws NoSuchAlgorithmException, InvalidKeySpecException {
		final SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		final byte encodedPBEKey[] = skf.generateSecret(spec).getEncoded();
		return new SecretKeySpec(encodedPBEKey, "AES");
	}
	
	public byte[] createEncryptionHeaderBytes() {
		final ByteBuffer bb = ByteBuffer.allocate(getEncryptHeaderSize());
		bb.putInt(1); // version
		bb.putInt(aesStrengthBits); // AES encryption, 128, 196 or 256
		bb.putInt(iterationCount);
		bb.put(salt);
		bb.put(ivseed);
		return bb.array();
	}
	
	static public int getEncryptHeaderSize() {
		return 12 + SALT_SIZE + IV_SEED_SIZE;
	}
	
	static public int getMaxAllowedAESKeyLength() throws NoSuchAlgorithmException {
		return Cipher.getMaxAllowedKeyLength("AES");
	}
}
