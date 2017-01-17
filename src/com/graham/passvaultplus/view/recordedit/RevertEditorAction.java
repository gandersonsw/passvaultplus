/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class RevertEditorAction extends AbstractAction {
	final private RecordEditContext editContext;

	public RevertEditorAction(final RecordEditContext editContextParam) {
		super("Revert");
		editContext = editContextParam;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			editContext.ignoreChangeEvents = true;
			editContext.populateUIFromRecord();
			editContext.setHasUnsavedChanges(false);
		} finally {
			editContext.ignoreChangeEvents = false;
		}
	}

}
