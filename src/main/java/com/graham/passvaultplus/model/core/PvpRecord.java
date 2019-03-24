/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.graham.passvaultplus.AppUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpContextUI;

public class PvpRecord {

	public final static String FILTER_ALL_CATEGORIES = "[All]";
	public final static String NO_CATEGORY = "[None]";

	private int id;
	private PvpRecord category;
	private Date creationDate;
	private Date modificationDate;
	private Map<String, String> fields = new HashMap<String, String>();
	private PvpType rtType;

	private String typeForValidate;
	private String categoryIdForValidate;

	PvpRecord() {
	}

	public PvpRecord(final PvpType t) {
		if (t == null) {
			throw new NullPointerException("type must be defined");
		}
		rtType = t;
	}

	public PvpType getType() {
		return rtType;
	}

	public int getId() {
		return id;
	}

	void setId(int idParam) {
		id = idParam;
	}

	public void clearId() {
		id = 0;
	}

	public PvpRecord getCategory() {
		return category;
	}

	public void setCategory(final PvpRecord categoryIdParam) {
		category = categoryIdParam;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(final Date d) {
		creationDate = d;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(final Date d) {
		modificationDate = d;
	}

	/**
	 * Get all the non-standard fields. "Notes" will be in here also.
	 */
	Map<String, String> getCustomFields() {
		return fields;
	}

	public String getCustomField(final String fieldName) {
		if (fieldName.equals(PvpField.USR_CATEGORY) ||
				fieldName.equals(PvpField.USR_CREATION_DATE) ||
				fieldName.equals(PvpField.USR_MODIFICATION_DATE) ||
				fieldName.equals(PvpField.USR_TYPE)) {
			throw new RuntimeException("dont call with:" + fieldName);
		}
/*
		if (!fields.containsKey(fieldName)) {
			PvpContext.getActiveUI().notifyInfo("PvpRecord.getCustomField :: id:" + id + " field not found:" + fieldName);
			StringBuffer sb = new StringBuffer();
			for (String fn : fields.keySet()) {
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(fn);
			}
			PvpContext.getActiveUI().notifyInfo("PvpRecord.getCustomField :: fields:" + sb);
		}
*/
		return fields.get(fieldName);
	}

	public void setCustomField(final String fieldName, final String fieldValue) {
		if (fieldName.equals(PvpField.USR_CATEGORY) ||
				fieldName.equals(PvpField.USR_CREATION_DATE) ||
				fieldName.equals(PvpField.USR_MODIFICATION_DATE) ||
				fieldName.equals(PvpField.USR_TYPE)) {
			throw new RuntimeException("dont call with:" + fieldName);
		}

		if (fieldValue == null) {
			fields.remove(fieldName);
		} else {
			fields.put(fieldName, fieldValue);
		}
	}

	/**
	 * Also include category, creation_date, modification_date, type
	 */
	Map<String, String> getAllFields() {
		Map<String, String> allFields = new HashMap<String, String>();
		allFields.putAll(fields);

		allFields.put(PvpField.USR_CATEGORY, category == null ? "" : String.valueOf(category.getId()));
		allFields.put(PvpField.USR_CREATION_DATE, AppUtil.formatDate1(creationDate));
		allFields.put(PvpField.USR_MODIFICATION_DATE, AppUtil.formatDate1(modificationDate));
		allFields.put(PvpField.USR_TYPE, rtType.getName());

		return allFields;
	}

	void setAnyField(final String fieldName, final String fieldValue) throws ParseException {
		if (fieldName.equals(PvpField.USR_CATEGORY)) {
			categoryIdForValidate = fieldValue;
		} else if (fieldName.equals(PvpField.USR_CREATION_DATE)) {
			try {
				creationDate = AppUtil.parseDate1(fieldValue);
			} catch (Exception e) {
				PvpContextUI.getActiveUI().notifyWarning("creation date parse error:" + fieldValue, e);
			}
		} else if (fieldName.equals(PvpField.USR_MODIFICATION_DATE)) {
			try {
				modificationDate = AppUtil.parseDate1(fieldValue);
			} catch (Exception e) {
				PvpContextUI.getActiveUI().notifyWarning("modification date parse error:" + fieldValue, e);
			}
		} else if (fieldName.equals(PvpField.USR_TYPE)) {
			typeForValidate = fieldValue;
		} else {
			fields.put(fieldName, fieldValue);
		}
	}

	public String toString() {
		if (rtType.getToStringCode() == null) {
			return super.toString();
		}
		return getCustomField(rtType.getToStringCode());
	}

	public String getFormated() {
		return rtType.getFullFormatter().format(this);
	}

	public String getFullText(boolean includeEmptyFields) {
		StringBuilder sb = new StringBuilder();
		sb.append("ID=");
		sb.append(this.getId());
		sb.append("; ");
		for (Map.Entry<String, String> entry : getAllFields().entrySet()) {
			if (!includeEmptyFields && entry.getValue().trim().length() > 0) {
				sb.append(entry.getKey());
				sb.append("=");
				PvpField f = rtType.getField(entry.getKey());
				if (f != null && f.isClassificationSecret()) {
					sb.append("***");
				} else {
					sb.append(entry.getValue());
				}
				sb.append("; ");
			}
		}
		return sb.toString();
	}

	/**
	 * Called after all fields have been set using setAnyField
	 */
	void initalizeAfterLoad(final PvpContext context, final PvpDataInterface dataInterface) {

		rtType = dataInterface.getType(typeForValidate);
		if (rtType == null) {
			context.ui.notifyWarning("WARN113 type not found:" + typeForValidate);
		}

		if (categoryIdForValidate == null || categoryIdForValidate.length() == 0) {
			category = null;
		} else {
			try {
				int catId = Integer.parseInt(categoryIdForValidate);
				category = dataInterface.getRecord(catId);
				if (category == null) {
					context.ui.notifyWarning("WARN114 for object id:" + id + " category not found:" + categoryIdForValidate);
				}
			} catch (final Exception e) {
				context.ui.notifyWarning("WARN115 for object id:" + id + " category not valid:" + categoryIdForValidate, e);
				category = null;
			}
		}

	}

	/**
	 * Return a match rating, 0 to 100
	 */
	public int matchRating(final PvpRecord otherRec) {
		if (otherRec == null) {
			return 0;
		}
		if (!PvpType.sameType(this.getType(), otherRec.getType())) {
			return 0;
		}
		int fieldCount = 5;
		int matchCount = 0;
		if (AppUtil.equalsWithNull(this.getCategory(), otherRec.getCategory())) {
			matchCount++;
		}
		if (AppUtil.equalsWithNull(this.getCreationDate(), otherRec.getCreationDate())) {// TODO this weight might be special
			matchCount += 4;
		}

		final Map<String, String> fields1 = this.getCustomFields();
		final Map<String, String> fields2 = otherRec.getCustomFields();

		final Set<String> keySet1 = fields1.keySet();
		final Set<String> keySet2 = fields2.keySet();
		final Set<String> allKeys = new HashSet<>();
		allKeys.addAll(keySet1);
		allKeys.addAll(keySet2);

		for (String k : allKeys) {
			String v1 = fields1.get(k);
			String v2 = fields2.get(k);
			if (v1 == null) {
				v1 = "";
			}
			if (v2 == null) {
				v2 = "";
			}
			// if both fields are "", pretend it doesn't exist. It doesn't count for or against the match rating
			if (v1.length() > 0 || v2.length() > 0) {
				fieldCount++;
				if (v1.equals(v2)) {
					matchCount++;
				}
			}
		}

		return 100 * matchCount / fieldCount;
	}

	/**
	 * @return true if otherRec was modified at all
	 */
	public boolean copyTo(PvpRecord otherRec) {
		if (otherRec == null) {
			System.out.println("PvpRecord.copyTo.A - cant copy to null");
			return false;
		}
		if (!PvpType.sameType(this.getType(), otherRec.getType())) {
			System.out.println("PvpRecord.copyTo.B - cant copy to other type");
			return false;
		}

		otherRec.setCategory(this.getCategory());
		otherRec.setCreationDate(this.getCreationDate());
		otherRec.setModificationDate(this.getModificationDate());

		final Map<String, String> fields1 = this.getCustomFields();
		final Map<String, String> fields2 = otherRec.getCustomFields();

		final Set<String> keySet1 = fields1.keySet();
		final Set<String> keySet2 = fields2.keySet();
		final Set<String> allKeys = new HashSet<>();
		allKeys.addAll(keySet1);
		allKeys.addAll(keySet2);

		boolean changed = false;
		for (String k : allKeys) {
			if (keySet1.contains(k)) {
				if (!AppUtil.equalsWithNull(fields1.get(k), fields2.get(k))) {
					changed = true;
					otherRec.setCustomField(k, fields1.get(k));
				}
			} else {
				final String v = fields2.get(k);
				if (v != null) {
					changed = true;
					otherRec.setCustomField(k, null);
				}
			}
		}
		return changed;
	}

	public boolean isPersisted() {
		return this.id > 0;
	}

}
