/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class EncryptionHeader {
	final static int HEADER_VERSION = 101;
	final static int SALT_SIZE = 16; // numbers of bytes of salt
	final static int IV_SEED_SIZE = 16; // Initialization Vector seed size
	
	final int iterationCount;
	final public int aesStrengthBits;
	final byte[] salt;
	final byte[] ivseed;
	
	EncryptionHeader(final byte[] encryptionHeaderBytes) {
		final ByteBuffer bb = ByteBuffer.wrap(encryptionHeaderBytes);
		int version = bb.getInt();
		if (version != HEADER_VERSION) {
			throw new RuntimeException("unsupported version number:" + version);
		}
		aesStrengthBits = bb.getInt();
		iterationCount = bb.getInt();
		salt = new byte[SALT_SIZE];
		bb.get(salt, 0, SALT_SIZE);
		ivseed = new byte[IV_SEED_SIZE];
		bb.get(ivseed, 0, IV_SEED_SIZE);
	}
	
	EncryptionHeader(final int aesStrengthBitsParam) throws NoSuchAlgorithmException {
		aesStrengthBits = aesStrengthBitsParam;
		iterationCount = 10000;
		final SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        salt = new byte[SALT_SIZE];
        sr.nextBytes(salt);
        
        ivseed = sr.generateSeed(IV_SEED_SIZE);
	}
	
	public byte[] createEncryptionHeaderBytes() {
		final ByteBuffer bb = ByteBuffer.allocate(getEncryptHeaderSize());
		bb.putInt(HEADER_VERSION); // version
		bb.putInt(aesStrengthBits); // AES encryption, 128, 196 or 256
		bb.putInt(iterationCount);
		bb.put(salt);
		bb.put(ivseed);
		return bb.array();
	}
	
	static public int getEncryptHeaderSize() {
		return 12 + SALT_SIZE + IV_SEED_SIZE;
	}

}
