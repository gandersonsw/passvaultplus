/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.ByteArrayInputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;

/**
 * Creates a database from a stream
 */
public class DatabaseReader {
	private final PvpContext context;
	private int maxID;

	public static PvpDataInterface read(final PvpContext contextParam, InputStream inStream) throws Exception {
		return new DatabaseReader(contextParam).readInternal(inStream);
	}
	
	private DatabaseReader(final PvpContext contextParam) {
		context = contextParam;
	}
	
	static class BadChar {
		final int theByte;
		final int theIndex;
		BadChar(int b, int i) {
			theByte = b;
			theIndex = i;
		}
	}
	
	private InputStream cleanStreamUTF8(InputStream inStream) throws IOException {
		byte[] goodData = new byte[1000000];
		int b;
		int goodDataCount = 0;
	
		List<BadChar> badChars = new ArrayList<>();
		while ((b = inStream.read()) != -1) {
			if (b < 128) {
				goodData[goodDataCount] = (byte) b;
				goodDataCount++;
			} else {
				badChars.add(new BadChar(b, goodDataCount));
			}
		}
		
		StringBuilder sb = new StringBuilder("There were bad characters in the file. Consider opening from a backup.\n");
		for (BadChar bc : badChars) {
			sb.append("\nChar:").append(bc.theByte);
			int start = bc.theIndex - 8;
			if (start < 0) {
				start = 0;
			}
			int length = 16;
			if (start + length >= goodDataCount) {
				length = goodDataCount - 1 - start;
			}
			sb.append(" Text:");
			sb.append(new String(goodData, start, length));
		}

		context.ui.showMessageDialog("Warning", sb.toString());

		return new ByteArrayInputStream(goodData, 0, goodDataCount);
	}
	
	private Document getJDom(InputStream inStream) throws JDOMException, IOException {
		// if builder.build throws an exception maybe there is some bad characters in the data?
		// in that case, call cleanStreamUTF8 to try to remove them and alert the user
		Document jdomDoc = null;
		final SAXBuilder builder = new SAXBuilder();
		try {
			if  (inStream.markSupported()) {
				inStream.mark(1000000);
			}
			jdomDoc = builder.build(inStream);
		} catch (CharConversionException originalException) {
			context.ui.notifyWarning("WARN119 builder.build CharConversionException", originalException);
			try {
				if  (inStream.markSupported()) {
					inStream.reset();
					inStream = cleanStreamUTF8(inStream);
					jdomDoc = builder.build(inStream);
				}
			} catch (Exception e2) {
				// if an exception happens, don't use it as the main exception, just log it
				context.ui.notifyWarning("cleanStreamUTF8 error", e2);
				throw originalException;
			}
		}
		return jdomDoc;
	}
	
	private PvpDataInterface readInternal(InputStream inStream) throws Exception {
		PvpDataInterface dataInterface;
		List<PvpType> types = null;
		List<PvpRecord> records = null;
		final Document jdomDoc = getJDom(inStream);
		final Element root = jdomDoc.getRootElement();
		if (!root.getName().equals("mydb")) {
			context.ui.notifyWarning("WARN101 unexpected element:" + root.getName());
		}
		final List children = root.getChildren();
		for (int i = 0; i < children.size(); i++) {
			Element e = (Element) children.get(i);
			if (e.getName().equals("types")) {
				types = loadTypes(e);
			} else if (e.getName().equals("records")) {
				records = loadRecords(e);
			} else {
				context.ui.notifyWarning("WARN102 unexpected element:" + e.getName());
			}
		}

		if (types == null) {
			throw new Exception("types XML element not found");
		}

		if (records == null) {
			throw new Exception("records XML element not found");
		}

		dataInterface = new PvpDataInterface(context, types, records, maxID);

		// do any initialization after all the data is loaded
		for (PvpRecord r : records) {
			r.initalizeAfterLoad(context, dataInterface);
		}

		return dataInterface;
	}

	private List<PvpType> loadTypes(final Element typesElement) {
		List children = typesElement.getChildren();
		List<PvpType> types = new ArrayList<PvpType>();
		Set<String> typeNames = new HashSet<String>();
		for (int i = 0; i < children.size(); i++) {
			Element e = (Element) children.get(i);
			if (!e.getName().equals("type")) {
				context.ui.notifyWarning("WARN103 unexpected element:" + e.getName());
			}
			PvpType type = loadOneType(e);
			if (typeNames.contains(type.getName())) {
				context.ui.notifyWarning("WARN104 duplicate type found, second definition ignored:" + type.getName());
			} else {
				types.add(type);
			}
		}
		return types;
	}

	private PvpType loadOneType(final Element typeElement) {
		List children = typeElement.getChildren();

		PvpType rttype = new PvpType();
		for (int i = 0; i < children.size(); i++) {
			Element e = (Element) children.get(i);
			if (e.getName().equals("name")) {
				rttype.setName(BCUtil.unmakeXMLSafe(e.getTextTrim()));
			} else if (e.getName().equals("to-string")) {
				rttype.setToStringCode(BCUtil.unmakeXMLSafe(e.getTextTrim()));
			} else if (e.getName().equals("full-format")) {
				rttype.setFullFormat(BCUtil.unmakeXMLSafe(e.getTextTrim()));
			} else if (e.getName().equals("field")) {
				rttype.addField(loadTypeField(e));
			} else {
				context.ui.notifyWarning("WARN105 unexpected element:" + e.getName());
			}
		}

		return rttype;
	}

	private PvpField loadTypeField(final Element fieldElement) {
		List children = fieldElement.getChildren();

		String classification = BCUtil.unmakeXMLSafe(fieldElement.getAttributeValue("classification"));
		if (classification != null && classification.trim().length() == 0) {
			classification = null;
		}

		String name = null;
		String type = null;
		for (int i = 0; i < children.size(); i++) {
			Element e = (Element) children.get(i);
			if (e.getName().equals("name")) {
				name = BCUtil.unmakeXMLSafe(e.getTextTrim());
			} else if (e.getName().equals("type")) {
				type = BCUtil.unmakeXMLSafe(e.getTextTrim());
			} else {
				context.ui.notifyWarning("WARN106 unexpected element:" + e.getName());
			}
		}
		
		if (name == null || type == null) {
			context.ui.notifyWarning("WARN107 name and type are required:" + fieldElement.getQualifiedName()); 
		}

		return new PvpField(name, type, classification);
	}

	private List<PvpRecord> loadRecords(final Element recordsElement) {
		maxID = 0;
		Attribute maxIdAttr = recordsElement.getAttribute("maxId");
		if (maxIdAttr != null) {
			try {
				maxID = maxIdAttr.getIntValue();
			} catch (DataConversionException dce) {
				context.ui.notifyWarning("WARN117", dce);
			}
		}
		if (maxID == 0) {
			context.ui.notifyWarning("maxID attribute not found");
		}
		List children = recordsElement.getChildren();
		List<PvpRecord> records = new ArrayList<PvpRecord>();
		for (int i = 0; i < children.size(); i++) {
			Element e = (Element) children.get(i);
			if (!e.getName().equals("record")) {
				context.ui.notifyWarning("WARN108 unexpected element:" + e.getName());
			}
			try {
				records.add(loadOneRecord(e));
			} catch (Exception ex) {
				context.ui.notifyWarning("WARN116", ex);
			}
		}

		return records;
	}

	private PvpRecord loadOneRecord(final Element recordElement) {
		List children = recordElement.getChildren();

		int id = -1;
		try {
			id = recordElement.getAttribute("id").getIntValue();
		} catch (Exception e) {
			context.ui.notifyWarning("WARN109 no id attribute for " + recordElement.getName(), e);
		}
		PvpRecord record = new PvpRecord();
		record.setId(id);
		for (int i = 0; i < children.size(); i++) {
			Element e = (Element) children.get(i);
			try {
				record.setAnyField(BCUtil.unmakeXMLName(e.getName()), BCUtil.unmakeXMLSafe(e.getText()));
			} catch (final Exception ex) {
				context.ui.notifyWarning("WARN110 loading id=" + id + " name:" + e.getName() + " text:" + e.getText(), ex);
			}
		}

		// note that record.initalizeAfterLoad will be called later
		if (record.getId() > maxID) {
			maxID = record.getId();
		}

		return record;
	}
}
