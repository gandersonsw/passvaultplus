/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordlist;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.graham.framework.CreateSortedTree;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.actions.EditRecordAction;
import com.graham.passvaultplus.actions.TextFieldChangeForwarder;
import com.graham.passvaultplus.model.CategoryTreeItemComparator;
import com.graham.passvaultplus.model.ListTableModel;
import com.graham.passvaultplus.model.RecordFilter;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.model.core.PvpType;
import com.graham.passvaultplus.view.*;

public class ViewListBuilder {

	static public Component buildViewList(final PvpContext context) {
		JPanel viewList = new JPanel(new BorderLayout());
		viewList.add(buildTopPanel(context), BorderLayout.NORTH);
		viewList.add(buildCenterPanel(context), BorderLayout.CENTER);
		return viewList;
	}

	static private Component buildTopPanel(final PvpContext context) {
		final ListFilterChangedAction filterChangeAction = new ListFilterChangedAction(context);

		final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

		panel.add(new JLabel("Filter:"));
		panel.add(buildFilterTextField(context, filterChangeAction));

		panel.add(new JLabel("Type:"));
		panel.add(buildTypeComboBox(context, filterChangeAction));

		panel.add(new JLabel("Category:"));
		panel.add(buildCategoryComboBox(context, filterChangeAction));
		
		JLabel rc = new JLabel("");
		context.getViewListContext().setRecordCountLabel(rc);
		panel.add(rc);

		return panel;
	}

	static private JComboBox buildTypeComboBox(final PvpContext context, final ListFilterChangedAction filterChangeAction) {
		List<PvpType> types = context.getDataInterface().getTypes();
		Object[] typeArray = new Object[types.size() + 1];
		typeArray[0] = PvpType.FILTER_ALL_TYPES;
		for (int i = 0; i < types.size(); i++) {
			typeArray[i+1] = types.get(i);
		}

		JComboBox typeCombo = new JComboBox(typeArray);
		typeCombo.addActionListener(filterChangeAction);
		context.getViewListContext().setTypeComboBox(typeCombo);
		return typeCombo;
	}

	static private void categoryHelper(final List categoryTree, final int level, final List<CategoryMenuItem> menuItems) {
		Iterator iter = categoryTree.iterator();
		while (iter.hasNext()) {
			Object item = iter.next();
			if (item instanceof PvpRecord) {
				menuItems.add(new CategoryMenuItem(((PvpRecord)item).getCustomField(PvpField.USR_CATEGORY_TITLE), level, (PvpRecord)item));
			} else {
				categoryHelper((List)item, level + 1, menuItems);
			}
		}
	}

	static private JComboBox buildCategoryComboBox(final PvpContext context, final ListFilterChangedAction filterChangeAction) {
		List<PvpRecord> categories = context.getDataInterface().getCategories();
		List categoryTree = CreateSortedTree.createSortedTree(categories, new CategoryTreeItemComparator());
		List<CategoryMenuItem> menuItems = new ArrayList<CategoryMenuItem>();
		menuItems.add(new CategoryMenuItem(PvpRecord.FILTER_ALL_CATEGORIES, 0, null));
		menuItems.add(new CategoryMenuItem(PvpRecord.NO_CATEGORY, 0, null));

		categoryHelper(categoryTree, 0, menuItems);

		JComboBox categoryCombo = new JComboBox(menuItems.toArray());
		categoryCombo.addActionListener(filterChangeAction);
		context.getViewListContext().setCategoryComboBox(categoryCombo);
		return categoryCombo;
	}

	static private JTextField buildFilterTextField(final PvpContext context, final ListFilterChangedAction filterChangeAction) {
		JTextField tf = new JTextField(12);
		TextFieldChangeForwarder f = new TextFieldChangeForwarder(filterChangeAction);
		tf.getDocument().addDocumentListener(f);
		context.getViewListContext().setFilterTextField(tf);
		return tf;
	}

	static private Component buildCenterPanel(final PvpContext context) {
		EditRecordAction erAction = new EditRecordAction(context);
		RecordFilter filter = new RecordFilter(context);
		ListTableModel model = new ListTableModel(filter);
		JTable table = new JTable(model);
		table.addMouseListener(new RecordListTableMouseAdpater(erAction));
		context.getViewListContext().setListTable(table, model);
		JScrollPane scroll = new JScrollPane(table);
		return scroll;
	}

}
