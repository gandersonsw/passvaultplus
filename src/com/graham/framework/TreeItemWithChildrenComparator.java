/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.framework;

import java.util.Comparator;

public class TreeItemWithChildrenComparator implements Comparator {

	Comparator subComp;
	
	public TreeItemWithChildrenComparator(Comparator subCompParam) {
		subComp = subCompParam;
	}

	public int compare(Object o1, Object o2) {
		return subComp.compare(((TreeItem)o1).item, ((TreeItem)o2).item); 
	}
	
}
