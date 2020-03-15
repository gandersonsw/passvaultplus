/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.view.longtask.LTManager;
import com.graham.util.ResourceUtil;

public class QuitAction extends AbstractAction {
	final PvpContext context;

	public QuitAction(final PvpContext c) {
		super(null, ResourceUtil.getIcon("exit"));
		context = c;
	}

	public void actionPerformed(ActionEvent e) {
		boolean shouldQuit = true;
		if (context.hasUnsavedChanges(false)) {
			boolean b = context.ui.showConfirmDialog("Delete", "There are some records that have been edited but not saved. \nAre you sure you want to quit?");
			if (!b) {
				shouldQuit = false;
			}
		}

		if (shouldQuit) {
			// TODO cancel not supported, maybe in future? test cancel
			LTManager.runWithProgress((ltr) -> {
				if (context.data.getFileInterface().appQuiting(ltr)) {
					System.exit(0);
				}
			}, "Saving");
		}
	}

}
