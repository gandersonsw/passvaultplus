/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import com.graham.passvaultplus.PvpContext;

public class CancelEditorAction extends AbstractAction {
	final private PvpContext context;
	final private RecordEditContext editContext;

	public CancelEditorAction(final PvpContext contextParam, RecordEditContext editContextParam) {
		super("Cancel");
		context = contextParam;
		editContext = editContextParam;
	}

	public void actionPerformed(ActionEvent e) {
		context.uiMain.removeRecordEditor(editContext);
	}

}
