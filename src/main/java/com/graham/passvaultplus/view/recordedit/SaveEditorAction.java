/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import com.graham.passvaultplus.PvpContext;

public class SaveEditorAction extends AbstractAction {
	final private PvpContext context;
	final private RecordEditContext editContext;

	public SaveEditorAction(final PvpContext contextParam, final RecordEditContext editContextParam) {
		super("Save");
		context = contextParam;
		editContext = editContextParam;
	}

	public void actionPerformed(ActionEvent e) {
		editContext.populateRecordFromUI(editContext.editRecord);
		context.getDataInterface().saveRecord(editContext.editRecord);
		context.getTabManager().removeRecordEditor(editContext);
	}

}
