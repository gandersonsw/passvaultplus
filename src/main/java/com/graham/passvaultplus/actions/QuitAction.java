/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

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
			int v = JOptionPane.showConfirmDialog(context.getMainFrame(), "There are some records that have been edited but not saved. Are you sure you want to quit?", "Unsaved changes", JOptionPane.OK_CANCEL_OPTION);
			if (v == JOptionPane.CANCEL_OPTION) {
				shouldQuit = false;
			}
		}

		if (shouldQuit) {
			System.exit(0);
		}
	}

	private boolean hasUnsavedChanges() {
		for (RecordEditContext editor : context.getTabManager().getRecordEditors()) {
			if (editor.hasUnsavedChanged()) {
				return true;
			}
		}
		return false;
	}

}
