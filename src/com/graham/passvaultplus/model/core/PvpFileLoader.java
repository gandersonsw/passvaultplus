/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Element;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;

public class PvpFileLoader {

	private PvpContext context;
	private int maxID;

	public PvpFileLoader(final PvpContext contextParam) {
		context = contextParam;
	}

	public int getMaxID() {
		return maxID;
	}

	public List<PvpType> loadTypes(final Element typesElement) {
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

	public List<PvpRecord> loadRecords(final Element recordsElement) {
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
			context.notifyBadException("no id attribute for " + recordElement.getName(), e, true);
		}
		PvpRecord record = new PvpRecord();
		record.setId(id);
		for (int i = 0; i < children.size(); i++) {
			Element e = (Element) children.get(i);
			try {
				record.setAnyField(BCUtil.unmakeXMLName(e.getName()), BCUtil.unmakeXMLSafe(e.getText()));
			} catch (final Exception ex) {
				context.notifyBadException("loading id=" + id + " name:" + e.getName() + " text:" + e.getText(), ex, true);
			}
		}

		// note that RtFileInterface will call validate on it later record.validate(context);

		if (record.getId() > maxID) {
			maxID = record.getId();
		}

		return record;
	}

}
