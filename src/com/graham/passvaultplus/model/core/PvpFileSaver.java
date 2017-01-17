/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.AppUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.UserAskToChangeFileException;

public class PvpFileSaver {

	class FileWriteState {
		FileOutputStream fileStream;
		ZipOutputStream zipStream;
		CipherOutputStream cipherStream;

		OutputStreamWriter writer;
		BufferedWriter bufWriter;

		OutputStream outStream; // do not close this, this is used as the last output
	}
	
	PvpContext context;
	BufferedWriter bw;

	public PvpFileSaver(final PvpContext contextParam) {
		context = contextParam;
	}
	
	public void save(final List<PvpType> types, final List<PvpRecord> records) {
		
		bw = null;
		FileWriteState fws = new FileWriteState();
		
		try {
			openWriter(fws);
			bw = fws.bufWriter;

			writeStart();
			writeTypes(types);
			writeRecords(records);
			writeEnd();
		} catch (final Exception e) {
			context.notifyBadException("cannot save file", e, true);
		} finally {
			closeWriter(fws);
		}
	}
	
	/**
	 * When writing, encryption happens first, then compression.
	 */
	private void openWriter(FileWriteState fws) throws IOException, UserAskToChangeFileException {
		final String path = context.getDataFilePath();
		File f = new File(path);

		AppUtil.checkBackupFileHourly(f);

		if (PvpFileInterface.isEncrypted(path)) {
			try {
				fws.cipherStream = new CipherOutputStream(new FileOutputStream(f), context.getFileInterface().getCipher(true, false));
				fws.outStream = fws.cipherStream;
				// write the code so we know it decrypted successfully
				fws.outStream.write("remthis7".getBytes());
			} catch (NoSuchAlgorithmException e) {
				context.notifyBadException("encrypting", e, true);
			} catch (NoSuchPaddingException e) {
				context.notifyBadException("encrypting", e, true);
			} catch (InvalidKeyException e) {
				context.notifyBadException("encrypting", e, true);
			}
		} else {
			fws.fileStream = new FileOutputStream(f);
			fws.outStream = fws.fileStream;
		}

		if (PvpFileInterface.isCompressed(path)) {
			fws.zipStream = new ZipOutputStream(fws.outStream);
			String zippedFileName = BCUtil.setFileExt(f.getName(), PvpFileInterface.isEncrypted(path) ? PvpFileInterface.EXT_ENCRYPT : PvpFileInterface.EXT_XML, true);
			fws.zipStream.putNextEntry(new ZipEntry(zippedFileName));
			fws.outStream = fws.zipStream;
		}

		fws.writer = new OutputStreamWriter(fws.outStream);
		fws.bufWriter = new BufferedWriter(fws.writer);
	}
	
	private void closeWriter(FileWriteState fws) {
		if (fws.bufWriter != null) {
			try {
				fws.bufWriter.write("               ");
				fws.bufWriter.flush();
			} catch (IOException e1) { }
			try {
				fws.bufWriter.close();
			} catch (IOException e1) { }
		}
		if (fws.cipherStream != null) {
			try {
				fws.cipherStream.flush();
			} catch (IOException e) { }
			try {
				fws.cipherStream.close();
			} catch (IOException e) { }
		}
		if (fws.zipStream != null) {
			try {
				fws.zipStream.closeEntry();
			} catch (IOException e) { }
			try {
				fws.zipStream.close();
			} catch (IOException e) { }
		}
		if (fws.fileStream != null) {
			try {
				fws.fileStream.close();
			} catch (IOException e) { }
		}
	}
	
	private void writeStart() throws IOException {
		bw.write("<mydb locale=\"en_US\">");
		bw.newLine();
	}
	
	private void writeTypes(final List<PvpType> types) throws IOException {
		bw.write("   <types>");
		bw.newLine();
		
		for (final PvpType t : types) {
			bw.write("      <type>");
			bw.newLine();
			bw.write("         <name>");
			bw.write(t.getName());
			bw.write("</name>");
			bw.newLine();
			bw.write("         <to-string>");
			bw.write(t.getToStringCode());
			bw.write("</to-string>");
			bw.newLine();
			
			writeTypeFields(t);
			
			bw.write("      </type>");
			bw.newLine();
		}
		
		bw.write("   </types>");
		bw.newLine();
	
	}
	
	private void writeTypeFields(final PvpType t) throws IOException {
		for (final PvpField f : t.getFields()) {
			bw.write("         <field>");
			bw.newLine();
			bw.write("            <name>");
			bw.write(f.getName());
			bw.write("</name>");
			bw.newLine();
			bw.write("            <type>");
			bw.write(f.getType());
			bw.write("</type>");
			bw.newLine();
			bw.write("         </field>");
			bw.newLine();
		}
	}
	
	private void writeRecords(final List<PvpRecord> records) throws IOException {
		bw.write("   <records>");
		bw.newLine();
		
		for (final PvpRecord r : records) {
			bw.write("      <record id=\"");
			bw.write(String.valueOf(r.getId()));
			bw.write("\">");
			bw.newLine();
			
			writeRecordFields(r);
			
			bw.write("      </record>");
			bw.newLine();
		}
		
		bw.write("   </records>");
		bw.newLine();
	}
	
	private void writeRecordFields(final PvpRecord r) throws IOException {

		for (Entry<String, String> entry : r.getAllFields().entrySet()) {
			bw.write("         <");
			final String name = BCUtil.makeXMLName((String)entry.getKey());
			bw.write(name);
			bw.write(">");
			bw.write(BCUtil.makeXMLSafe(entry.getValue()));
			bw.write("</");
			bw.write(name);
			bw.write(">");
			bw.newLine();
		}
	}
	
	private void writeEnd() throws IOException {
		bw.write("</mydb>");
		bw.newLine();
	}
	
	//private File getXmlFile() {
	//	return new File(context.getDataFilePath());
	//}
	

}
