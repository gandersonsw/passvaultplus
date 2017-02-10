/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;

/**
 * Write the database out to a stream
 */
public class DatabaseWriter {
	final PvpContext context;
	final BufferedWriter bw;

	static public void write(final PvpContext contextParam, final BufferedWriter outStream, final PvpDataInterface dataInterface) throws IOException {
		new DatabaseWriter(contextParam, outStream).writeInternal(dataInterface);
	}
	
	private DatabaseWriter(final PvpContext contextParam, final BufferedWriter outStream) {
		context = contextParam;
		bw = outStream;
	}

	private void writeInternal(final PvpDataInterface dataInterface) throws IOException {
		// final List<PvpType> types, final List<PvpRecord> records

		//bw = null;
		//FileWriteState fws = new FileWriteState();

	//	try {
		//	openWriter(fws);
			//bw = fws.bufWriter;

		writeStart();
		writeTypes(dataInterface.getTypes());
		writeRecords(dataInterface.getRecords());
		writeEnd();
	//	} catch (final Exception e) {
	//		context.notifyBadException("cannot save file", e, true);
	//	} finally {
	//		closeWriter(fws);
	//	}
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
			if (t.getFullFormat() != null) {
				bw.write("         <full-format>");
				bw.write(t.getToStringCode());
				bw.write("</full-format>");
				bw.newLine();
			}
			writeTypeFields(t);

			bw.write("      </type>");
			bw.newLine();
		}

		bw.write("   </types>");
		bw.newLine();
	}

	private void writeTypeFields(final PvpType t) throws IOException {
		for (final PvpField f : t.getFields()) {
			bw.write("         <field");
			if (f.getClassification() != null) {
				bw.write(" classification=\"");
				bw.write(f.getClassification());
				bw.write("\"");
			}
			bw.write(">");
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
			final String name = BCUtil.makeXMLName((String) entry.getKey());
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
}
