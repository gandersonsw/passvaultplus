/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import com.graham.passvaultplus.model.core.PvpRecord;

public class CategoryMenuItem {

	private String title;
	final private PvpRecord category;
	
	public CategoryMenuItem(final String titleParam, final int levelParam, final PvpRecord categoryParam) {
		title = titleParam;
		for (int i = 0; i < levelParam; i++) {
			title = "    " + title;
		}
		category = categoryParam;
	}
	
	public String toString() {
		return title;
	}
	
	public PvpRecord getCategory() {
		return category;
	}
	
}
