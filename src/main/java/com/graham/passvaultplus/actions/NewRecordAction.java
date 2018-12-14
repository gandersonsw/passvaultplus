/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.model.core.PvpType;
import com.graham.passvaultplus.view.recordedit.RecordEditBuilder;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;

public class NewRecordAction extends AbstractAction {
	final private PvpContext context;

	public NewRecordAction(final PvpContext contextParam) {
		super(null, PvpContext.getIcon("new"));
		context = contextParam;
	}

	public void actionPerformed(ActionEvent e) {
		final PvpType type = getTypeToCreateNew();
		if (type == null) {
			JOptionPane.showMessageDialog(null, "A Type must be selected first.");
			return; // can't create a new record if there are no types defined
		}
		PvpRecord r = new PvpRecord(type);
		final RecordEditContext editor = RecordEditBuilder.buildEditor(context, r, true);
		context.ui.getTabManager().addRecordEditor("new", editor);
		context.ui.getTabManager().setSelectedComponent(editor.getPanelInTabPane());
	}

	private PvpType getTypeToCreateNew() {
		Object selItem = context.ui.getViewListContext().getTypeComboBox().getSelectedItem();
		if (PvpType.FILTER_ALL_TYPES.equals(selItem)) {
			for (int i = 0 ; i < context.ui.getViewListContext().getTypeComboBox().getItemCount(); i++) {
				Object item = context.ui.getViewListContext().getTypeComboBox().getItemAt(i);
				if (item instanceof PvpType) {
					return (PvpType)item;
				}
			}
		} else {
			return (PvpType)selItem;
		}
		return null;
	}

}
