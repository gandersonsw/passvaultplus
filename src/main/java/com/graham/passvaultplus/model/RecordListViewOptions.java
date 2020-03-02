/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import java.util.List;
import java.util.Set;

import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpType;

public class RecordListViewOptions {
	public static final int VTL_NEVER = 1; // never use the vertical table layout
	public static final int VTL_ONLY_1 = 2; // only use the vertical table layout if there is 1 record
	public static final int VTL_LT_5 = 3; // only use the vertical table layout if there is less than 5 records
	public static final int VTL_GT_1_TYPE = 4; // always use the vertical table layout if there are records of different types
	public static final int VTL_ALWAYS = 5; // always use the vertical table layout

	public static final int SORT_ASC = 1;
	public static final int SORT_DESC = 2;

	private boolean fieldVisible[] = new boolean[5];

	private int vertTableLayout = VTL_GT_1_TYPE;

	private PvpType sortOriginType = null;
	private PvpField sort = null;
	private int sortDir = 0;

	public void setVertTableLayout(int i) {
		if (i < 1 || i > 5) {
			throw new RuntimeException("Invalid vertTableLayout value:" + i);
		}
		vertTableLayout = i;
	}

	public int getVertTableLayout() {
		return vertTableLayout;
	}

	public void setSort(PvpType typeParam, PvpField sfParam, int sortOrder) {
		if (sfParam == null) {
			sortOriginType = null;
			sort = null;
			return;
		}
		if (sfParam.getName().indexOf(',') != -1) {
			throw new RuntimeException("Comma not allowed in sort string");
		}
		if (sortOrder != SORT_ASC && sortOrder != SORT_DESC) {
			throw new RuntimeException("Invalid sortOrder value:" + sortOrder);
		}
		sortOriginType = typeParam;
		sort = sfParam;
		sortDir = sortOrder;
	}

	public PvpField getSort() {
		return sort;
	}

	public int getSortDir() {
		return sortDir;
	}

	/**
	 * @return True if the field should be shown in the UI.
	 */
	public boolean shouldShowField(PvpField f) {
		return getFieldVisible(f, false);
	}

	public void addCommonFields(List<PvpField> typeFields) {
		for (int i = 0; i < fieldVisible.length; i++) {
			if (fieldVisible[i]) {
				typeFields.add(PvpField.getCoreField(mapIndexToCfid(i)));
			}
		}
	}

	public void addCommonFields(List<PvpField> typeFields, Set<Integer> excludeCfids) {
		for (int i = 0; i < fieldVisible.length; i++) {
			if (fieldVisible[i]) {
				PvpField f = PvpField.getCoreField(mapIndexToCfid(i));
				if (!excludeCfids.contains(f.getCoreFieldId())) {
					typeFields.add(f);
				}
			}
		}
	}

	/**
	 * @return True if the field is configured to be shown. Throw an exception if it is a field that is not configurable.
	 */
	public boolean getFieldVisible(PvpField field) {
		return getFieldVisible(field, true);
	}

	private boolean getFieldVisible(PvpField field, boolean throwOnOther) {
		try {
			return fieldVisible[mapCfidToIndex(field.getCoreFieldId())];
		} catch (IndexOutOfBoundsException e) {
			if (throwOnOther) {
				throw new RuntimeException("Invalid field:" + field.getName());
			} else {
				return true;
			}
		}
	}

	public void setFieldVisible(PvpField field, boolean val) {
		fieldVisible[mapCfidToIndex(field.getCoreFieldId())] = val;
		//throw new RuntimeException("Invalid fieldName value:" + field.getName());
	}

	public boolean toggleFieldVisible(PvpField field) {
		boolean newVal = !getFieldVisible(field);
		setFieldVisible(field, newVal);
		return newVal;
	}

	public String toString() {
		return "v1," + fieldVisible[0] + "," + fieldVisible[1] + "," + fieldVisible[2] + "," +
				fieldVisible[3] + "," + fieldVisible[4] + "," + vertTableLayout + "," + String.valueOf(sortDir) + sort.getName();
	}
/*
TODO - need to possibly look up type? and check the CFID ?
	public void parseFromString(String s) {
		String parts[] = s.split(",");
		if (!parts[0].equals("v1")) {
			throw new RuntimeException("expected version to be v1");
		}
		fieldVisible[0] = Boolean.parseBoolean(parts[1]);
		fieldVisible[1] = Boolean.parseBoolean(parts[2]);
		fieldVisible[2] = Boolean.parseBoolean(parts[3]);
		fieldVisible[3] = Boolean.parseBoolean(parts[4]);
		fieldVisible[4] = Boolean.parseBoolean(parts[5]);
		vertTableLayout = Integer.parseInt(parts[6]);
		sortDir = Integer.parseInt(parts[7].substring(0, 1));
		sort = parts[7].substring(1);
	}
*/
	/**
	 * Map Core Field Id to index of fieldVisible.
	 */
	private int mapCfidToIndex(int cfid) {
		return cfid - PvpField.FIRST_CFID;
	}

	/**
	 * Map index of fieldVisible to Core Field Id.
	 */
	private int mapIndexToCfid(int index) {
		return index + PvpField.FIRST_CFID;
	}

}
