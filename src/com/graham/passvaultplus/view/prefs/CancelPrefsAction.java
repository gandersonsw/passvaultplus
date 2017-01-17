/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.PvpContext;

public class CancelPrefsAction extends AbstractAction {
	final private PvpContext context;

	public CancelPrefsAction(final PvpContext contextParam) {
		super("Cancel");
		context = contextParam;
	}

	public void actionPerformed(ActionEvent e) {
		context.getTabManager().removeOtherTab(context.getPrefsComponent());
		context.setPrefsComponent(null);
	}

}
