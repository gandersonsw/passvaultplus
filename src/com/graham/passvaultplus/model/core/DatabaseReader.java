/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

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
	
	private PvpDataInterface readInternal(InputStream inStream) throws Exception {
		PvpDataInterface dataInterface;
		List<PvpType> types = null;
		List<PvpRecord> records = null;
		final SAXBuilder builder = new SAXBuilder();
		final Document jdomDoc = builder.build(inStream);
		final Element root = jdomDoc.getRootElement();
		if (!root.getName().equals("mydb")) {
			context.notifyWarning("unexpected element:" + root.getName());
		}
		System.out.println("data file locale=" + root.getAttribute("locale"));
		final List children = root.getChildren();
		for (int i = 0; i < children.size(); i++) {
			Element e = (Element) children.get(i);
			if (e.getName().equals("types")) {
				types = loadTypes(e);
			} else if (e.getName().equals("records")) {
				records = loadRecords(e);
			} else {
				context.notifyWarning("unexpected element:" + e.getName());
			}
		}

		if (types == null) { // TODO test this , this should be handled better
			throw new Exception("type data not found"); // PvpException
		}

		if (records == null) { // TODO this should be handled better
			throw new Exception("record data not found");
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
				context.notifyWarning("unexpected element:" + e.getName());
			}
			PvpType type = loadOneType(e);
			if (typeNames.contains(type.getName())) {
				context.notifyWarning("duplicate type found, second definition ignored:" + type.getName());
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
				context.notifyWarning("unexpected element:" + e.getName());
			}
		}

		return rttype;
	}

	private PvpField loadTypeField(final Element fieldElement) {
		List children = fieldElement.getChildren();

		String classification = fieldElement.getAttributeValue("classification");
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
				context.notifyWarning("unexpected element:" + e.getName());
			}
		}

		if (name == null || type == null) {
			// TODO handle this better
			context.notifyWarning("name and type are required:" + fieldElement.getQualifiedName()); 
		}

		return new PvpField(name, type, classification);
	}

	private List<PvpRecord> loadRecords(final Element recordsElement) {
		maxID = 0;
		List children = recordsElement.getChildren();
		List<PvpRecord> records = new ArrayList<PvpRecord>();
		for (int i = 0; i < children.size(); i++) {
			Element e = (Element) children.get(i);
			if (!e.getName().equals("record")) {
				context.notifyWarning("unexpected element:" + e.getName());
			}
			try {
				records.add(loadOneRecord(e));
			} catch (Exception ex) {
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
			context.notifyWarning("no id attribute for " + recordElement.getName(), e);
			//context.notifyBadException("no id attribute for " + recordElement.getName(), e, true);
		}
		PvpRecord record = new PvpRecord();
		record.setId(id);
		for (int i = 0; i < children.size(); i++) {
			Element e = (Element) children.get(i);
			try {
				record.setAnyField(BCUtil.unmakeXMLName(e.getName()), BCUtil.unmakeXMLSafe(e.getText()));
			} catch (final Exception ex) {
				context.notifyWarning("loading id=" + id + " name:" + e.getName() + " text:" + e.getText(), ex);
			}
		}

		// TODO update this       note that RtFileInterface will call validate on it later record.validate(context);

		if (record.getId() > maxID) {
			maxID = record.getId();
		}

		return record;
	}
}
