/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.util.Comparator;
import java.util.Date;

import com.graham.passvaultplus.AppUtil;

public class PvpRecordComparator implements Comparator<PvpRecord> {

	private final PvpField sortField;
	private final int cfid;
	private final boolean asc;
	private final int pos1; // when asc, = 1, else -1
	private final int neg1; // when asc, = -1, else 1

	public PvpRecordComparator(PvpField fieldParam, boolean ascendingParam) {
		sortField = fieldParam;
		cfid = fieldParam.getCoreFieldId();
		asc = ascendingParam;
		pos1 = asc ? 1 : -1;
		neg1 = asc ? -1 : 1;
	}

	public int compare(PvpRecord r1, PvpRecord r2) {
		int retVal = 0;
		switch (cfid) {
			case PvpField.CFID_CATEGORY:
				String c1 = r1.getCategory() == null ? null : r1.getCategory().getCustomField(PvpField.USR_CATEGORY_TITLE);
				String c2 = r2.getCategory() == null ? null : r2.getCategory().getCustomField(PvpField.USR_CATEGORY_TITLE);
				retVal = compareStrings(c1, c2);
				break;
			case PvpField.CFID_CREATION_DATE:
				retVal = compareDates(r1.getCreationDate(), r2.getCreationDate());
				break;
			case PvpField.CFID_MODIFICATION_DATE:
				retVal = compareDates(r1.getModificationDate(), r2.getModificationDate());
				break;
			case PvpField.CFID_NOTES:
				retVal = compareStrings(r1.getCustomField(PvpField.CF_NOTES.getName()), r2.getCustomField(PvpField.CF_NOTES.getName()));
				break;
			case PvpField.CFID_TYPE:
				retVal = compareStrings(r1.getType().getName(), r2.getType().getName());
				break;
			case PvpField.CFID_IDENTITY:
				retVal = Integer.compare(r1.getId(), r2.getId());
				break;
			case PvpField.CFID_UNDEF:
				if (sortField.isTypeDate()) {
					retVal = compareDates(r1.getCustomField(sortField.getName()), r2.getCustomField(sortField.getName()));
				} else {
					retVal = compareStrings(r1.getCustomField(sortField.getName()), r2.getCustomField(sortField.getName()));
				}
				break;
		}

		if (!asc) {
			retVal = -retVal;
		}
		
		return retVal;
	}

	int compareDates(String s1, String s2) {
		Date d1 = null;
		Date d2 = null;
		try { d1 = AppUtil.parseDate2(s1); } catch (Exception e) { }
		try { d2 = AppUtil.parseDate2(s2); } catch (Exception e) { }

		if (d1 == null && d2 == null) {
			return compareStrings(s1, s2);
		} else {
			return compareDates(d1, d2);
		}
	}

	int compareDates(Date d1, Date d2) {
		if (d1 == null && d1 == null) {
			return 0;
		} else if (d1 == null) {
			return pos1;
		} else if (d2 == null) {
			return neg1;
		} else {
			return d1.compareTo(d2);
		}
	}

	int compareStrings(String s1, String s2) {
		if (s1 == null || s1.length() == 0) {
			if (s2 == null || s2.length() == 0) {
				return 0;
			} else {
				return pos1;
			}
		} else {
			if (s2 == null || s2.length() == 0) {
				return neg1;
			} else {
				return s1.compareToIgnoreCase(s2);
			}
		}
	}
}
