/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;

public class QuitAction extends AbstractAction {
	final PvpContext context;

	public QuitAction(final PvpContext c) {
		super(null, PvpContext.getIcon("exit"));
		context = c;
	}

	public void actionPerformed(ActionEvent e) {
		boolean shouldQuit = true;
		if (hasUnsavedChanges()) {
			boolean b = context.ui.showConfirmDialog("Delete", "There are some records that have been edited but not saved. \nAre you sure you want to quit?");
			if (!b) {
				shouldQuit = false;
			}
		}

		if (shouldQuit) {
			if (context.data.getFileInterface().appQuiting()) {
				System.exit(0);
			}
		}
	}

	private boolean hasUnsavedChanges() {
		for (RecordEditContext editor : context.uiMain.getRecordEditors()) {
			if (editor.hasUnsavedChanged()) {
				return true;
			}
		}
		return false;
	}

}
