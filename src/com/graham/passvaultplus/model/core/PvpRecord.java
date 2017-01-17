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
			System.out.println("2 dont call with:" + fieldName);
			new Exception().printStackTrace();
			throw new RuntimeException("dont call with:" + fieldName);
		}

		if (!fields.containsKey(fieldName)) {
			// TODO System.out.println("id:" + id + " field not found:" + fieldName);
			StringBuffer sb = new StringBuffer();
			for (String fn : fields.keySet()) {
				if (sb.length() > 0) {
					sb.append(',');
				}
				sb.append(fn);
			}
			// TODO System.out.println("fields:" + sb);
		}

		return fields.get(fieldName);
	}

	public void setCustomField(final String fieldName, final String fieldValue) {
		if (fieldName.equals(PvpField.USR_CATEGORY) ||
				fieldName.equals(PvpField.USR_CREATION_DATE) ||
				fieldName.equals(PvpField.USR_MODIFICATION_DATE) ||
				fieldName.equals(PvpField.USR_TYPE)) {
			System.out.println("1 dont call with:" + fieldName);
			new Exception().printStackTrace();
			throw new RuntimeException("dont call with:" + fieldName);
		}
		fields.put(fieldName, fieldValue);
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
				System.out.println("creation date parse error:" + fieldValue);
			}
		} else if (fieldName.equals(PvpField.USR_MODIFICATION_DATE)) {
			try {
				modificationDate = AppUtil.parseDate1(fieldValue);
			} catch (Exception e) {
				System.out.println("modification date parse error:" + fieldValue);
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
		return (String)getCustomField(rtType.getToStringCode());
	}

  //	String getTypeForValidate() {
//		return typeForValidate;
//	}

	//String getCategoryIdForValidate() {
	//	return categoryIdForValidate;
	//}

	/**
	 * Called after all fields have been set using setAnyField
	 */
	void initalizeAfterLoad(final PvpContext context, final PvpDataInterface dataInterface) {

		rtType = dataInterface.getType(typeForValidate);
		if (rtType == null) {
			context.notifyWarning("type not found:" + typeForValidate);
		}

		if (categoryIdForValidate == null || categoryIdForValidate.length() == 0) {
			category = null;
		} else {
			try {
				int catId = Integer.parseInt(categoryIdForValidate);
				category = dataInterface.getRecord(catId);
				if (category == null) {
					context.notifyWarning("for object id:" + id + " category not found:" + categoryIdForValidate);
				}
			} catch (final Exception e) {
				e.printStackTrace();
				context.notifyWarning("for object id:" + id + " category not valid:" + categoryIdForValidate);
				category = null;
			}
		}

	}


	public boolean isSimilar(PvpRecord otherRec) {
		if (otherRec == null) {
			return false;
		}
		if (otherRec.getType() == null) {
			return false;
		}
		if (this.getType() == null) {
			return false;
		}
		if (!otherRec.getType().getName().equals(this.getType().getName())) {
			return false;
		}
		if (!AppUtil.equalsWithNull(this.getCategory(), otherRec.getCategory())) {
			return false;
		}

		final Map<String, String> fields1 = this.getCustomFields();
		final Map<String, String> fields2 = otherRec.getCustomFields();

		final Set<String> keySet1 = fields1.keySet();
		final Set<String> keySet2 = fields2.keySet();
		final Set<String> allKeys = new HashSet();
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
			//System.out.println("equals2 " + k + " >" + v1 + "=" + v2 + "<");
			if (!v1.equals(v2)) {
				return false;
			}
		}

		return true;
	}

	public boolean isPersisted() {
		return this.id > 0;
	}

/*
	public int hashCode2() {
		if (id == 0) {
			StringBuilder sb = new StringBuilder();
			for (String s : fields.values()) {
				if (s != null) {
					sb.append(s);
				}
			}
			return sb.hashCode();
		}
		return id;
	}
*/
}
