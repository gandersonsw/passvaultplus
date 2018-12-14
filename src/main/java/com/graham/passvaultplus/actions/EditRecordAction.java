/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.AppUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.view.recordedit.RecordEditBuilder;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;

public class EditRecordAction extends AbstractAction {
	private final PvpContext context;

	public EditRecordAction(final PvpContext contextParam) {
		super(null, PvpContext.getIcon("edit"));
		context = contextParam;
	}

	public void actionPerformed(ActionEvent e) {
		PvpRecord r = context.ui.getViewListContext().getFirstSelectedRecord();
		if (r != null) {
			RecordEditContext editor = context.ui.getTabManager().getRecordEditor(r);
			if (editor == null) {
				editor = RecordEditBuilder.buildEditor(context, r, false);
				context.ui.getTabManager().addRecordEditor(AppUtil.limitStrLen(r.toString(), 30), editor);
				
			}
			context.ui.getTabManager().setSelectedComponent(editor.getPanelInTabPane());
		}
	}

}
