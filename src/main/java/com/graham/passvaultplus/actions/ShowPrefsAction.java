/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.view.prefs.PreferencesBuilder;
import com.graham.passvaultplus.view.prefs.PreferencesConnectionTab;

public class ShowPrefsAction extends AbstractAction {
	final private PvpContext context;

	public ShowPrefsAction(final PvpContext contextParam) {
		super(null, PvpContext.getIcon("settings"));
		context = contextParam;
	}

	public void actionPerformed(ActionEvent e) {
		Component c = context.ui.getPrefsComponent();
		if (c == null) {
			c = PreferencesBuilder.buildPrefs(new PreferencesConnectionTab(context));
			//c = new JScrollPane(npc);
			context.ui.setPrefsComponent(c);
			context.ui.getTabManager().addOtherTab("Settings", c);
		}
		context.ui.getTabManager().setSelectedComponent(c);
	}

}
