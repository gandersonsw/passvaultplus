/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.framework;

import java.util.ArrayList;
import java.util.List;

public class TreeItem {
	public Object item; // This is the orginal object type
	public Object parent; // This si the orginal object type
	public List<TreeItem> children = new ArrayList<TreeItem>(); // this is a List of TreeItem
	
	public TreeItem(final Object itemParam, final Object parentParam) {
		item = itemParam;
		parent = parentParam;
	}
}
