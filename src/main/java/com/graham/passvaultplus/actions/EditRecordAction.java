/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpRecord;

public class EditRecordAction extends AbstractAction {
	private final PvpContext context;

	public EditRecordAction(final PvpContext contextParam) {
		super(null, PvpContext.getIcon("edit"));
		context = contextParam;
	}

	public void actionPerformed(ActionEvent e) {
		PvpRecord r = context.uiMain.getViewListContext().getFirstSelectedRecord();
		context.uiMain.addRecordEditorIfNeeded(r);
	}

}
