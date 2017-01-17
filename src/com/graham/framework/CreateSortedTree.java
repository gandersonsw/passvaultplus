/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.graham.passvaultplus.model.core.PvpRecord;

public class CreateSortedTree {

	final private TreeItemComparator ti;
	final private TreeItemWithChildrenComparator comp2;
	final private List<TreeItem> itemsWithChildren = new ArrayList<TreeItem>();

	private CreateSortedTree(TreeItemComparator tiParam) {
		ti = tiParam;
		comp2 = new TreeItemWithChildrenComparator(ti);
	}

	public static List createSortedTree(final List<PvpRecord> items, final TreeItemComparator ti) {
		return new CreateSortedTree(ti).createSortedTree1(items);
	}

	private List createSortedTree1(final List items) {

		Iterator iter = items.iterator();
		while (iter.hasNext()) {
			Object item = iter.next();
			Object parent = ti.getParent(item);
			itemsWithChildren.add(new TreeItem(item, parent));
		}
		
		// now have List initlized, with item and parent set
		// now set up the children
		
		iter = itemsWithChildren.iterator();
		while (iter.hasNext()) {
			TreeItem item = (TreeItem)iter.next();
			if (item.parent != null) {
				getItemWithChildren(item.parent).children.add(item);
			}
		}

		// now create a root, with all items that don't have parents
		TreeItem root = new TreeItem(null, null);
		iter = itemsWithChildren.iterator();
		while (iter.hasNext()) {
			TreeItem i = (TreeItem)iter.next();
			if (i.parent == null) {
				root.children.add(i);
			}
		}
		
		return sortAndSimplify(root);
	}
	
	private List sortAndSimplify(final TreeItem item) {
		Object arr[] = item.children.toArray();
		Arrays.sort(arr, comp2);
		List ret = new ArrayList();
		for (int i = 0; i < arr.length; i++) {
			TreeItem child = ((TreeItem)arr[i]);
			ret.add(child.item);
			if (!item.children.isEmpty()) {
				ret.add(sortAndSimplify(child));
			}
		}
		return ret;
	}
	
	private TreeItem getItemWithChildren(Object item) {
		//Iterator iter = itemsWithChildren.iterator();
		for (TreeItem i : itemsWithChildren) {
			//TreeItem i = (TreeItem)iter.next();
			if (ti.compare(i.item, item) == 0) {
				return i;
			}
		}
		return null;
	}
	
}
