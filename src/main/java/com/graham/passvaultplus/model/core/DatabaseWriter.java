/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.graham.passvaultplus.PvpContext;
import com.graham.util.XmlUtil;

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
		writeStart();
		writeTypes(dataInterface.getTypes());
		writeRecords(dataInterface.getRecords(), dataInterface.getMaxId());
		writeMetadata(dataInterface.getMetadataMap());
		writeEnd();
	}
	
	private void writeStart() throws IOException {
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		bw.newLine();
		bw.write("<mydb locale=\"en_US\">");
		bw.newLine();
	}

	private void writeTypes(final List<PvpType> types) throws IOException {
		bw.write("	<types>");
		bw.newLine();

		for (final PvpType t : types) {
			bw.write("		<type>");
			bw.newLine();
			bw.write("			<name>");
			bw.write(XmlUtil.makeXMLSafe(t.getName()));
			bw.write("</name>");
			bw.newLine();
			bw.write("			<to-string>");
			bw.write(XmlUtil.makeXMLSafe(t.getToStringCode()));
			bw.write("</to-string>");
			bw.newLine();
			if (t.getFullFormat() != null && t.getFullFormat().length() > 0) {
				bw.write("			<full-format>");
				bw.write(XmlUtil.makeXMLSafe(t.getFullFormat()));
				bw.write("</full-format>");
				bw.newLine();
			}
			writeTypeFields(t);

			bw.write("		</type>");
			bw.newLine();
		}

		bw.write("	</types>");
		bw.newLine();
	}

	private void writeTypeFields(final PvpType t) throws IOException {
		for (final PvpField f : t.getFields()) {
			bw.write("			<field");
			if (f.getClassification() != null) {
				bw.write(" classification=\"");
				bw.write(XmlUtil.makeXMLSafe(f.getClassification()));
				bw.write("\"");
			}
			bw.write(">");
			bw.newLine();
			bw.write("				<name>");
			bw.write(XmlUtil.makeXMLSafe(f.getName()));
			bw.write("</name>");
			bw.newLine();
			bw.write("				<type>");
			bw.write(XmlUtil.makeXMLSafe(f.getType()));
			bw.write("</type>");
			bw.newLine();
			bw.write("			</field>");
			bw.newLine();
		}
	}

	private void writeRecords(final List<PvpRecord> records, final int maxId) throws IOException {
		bw.write("	<records maxId=\"");
		bw.write(String.valueOf(maxId));
		bw.write("\">");
		bw.newLine();

		for (final PvpRecord r : records) {
			bw.write("		<record id=\"");
			bw.write(String.valueOf(r.getId()));
			bw.write("\">");
			bw.newLine();

			writeRecordFields(r);

			bw.write("		</record>");
			bw.newLine();
		}

		bw.write("	</records>");
		bw.newLine();
	}

	private void writeRecordFields(final PvpRecord r) throws IOException {

		for (Entry<String, String> entry : r.getAllFields().entrySet()) {
			bw.write("			<");
			final String name = XmlUtil.makeXMLName((String) entry.getKey());
			bw.write(name);
			bw.write(">");
			bw.write(XmlUtil.makeXMLSafe(entry.getValue()));
			bw.write("</");
			bw.write(name);
			bw.write(">");
			bw.newLine();
		}
	}

	private void writeMetadata(final Map<String, String> data) throws IOException {
		if (data == null || data.isEmpty()) {
			return;
		}
		bw.write("	<metadata>");
		bw.newLine();
		for (Entry<String, String> entry : data.entrySet()) {
			bw.write("		<entry name=\"");
			final String name = XmlUtil.makeXMLName((String) entry.getKey());
			bw.write(name);
			bw.write("\">");
			bw.write(XmlUtil.makeXMLSafe(entry.getValue()));
			bw.write("</entry>");
			bw.newLine();
		}
		bw.write("	</metadata>");
		bw.newLine();
	}

	private void writeEnd() throws IOException {
		bw.write("</mydb>");
		bw.newLine();
	}
}
