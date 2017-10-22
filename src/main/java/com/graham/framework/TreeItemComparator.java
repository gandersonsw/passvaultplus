/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.framework;

import java.util.Comparator;

public interface TreeItemComparator extends Comparator {
	
	/**
	 * Can return null if the item does not have a parent. Multiple items may not have a parent.
	 */
	Object getParent(Object arg0);

}
