/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model;

import com.graham.framework.TreeItemComparator;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;

public class CategoryTreeItemComparator implements TreeItemComparator {

	public int compare(Object arg0, Object arg1) {
		final PvpRecord r0 = (PvpRecord)arg0;
		final PvpRecord r1 = (PvpRecord)arg1;
		final String title0 = r0.getCustomField(PvpField.USR_CATEGORY_TITLE);
		final String title1 = r1.getCustomField(PvpField.USR_CATEGORY_TITLE);
		final int c = title0.compareTo(title1);
		if (c == 0) {
			if (r0.getId() > r1.getId()) {
				return -1;
			} else if (r0.getId() < r1.getId()) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return c;
		}
	}
	
	public Object getParent(Object arg0) {
		final PvpRecord r0 = (PvpRecord)arg0;
		return r0.getCategory();
	}

}
