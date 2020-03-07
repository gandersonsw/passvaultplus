/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.view.OtherTab;
import com.graham.util.ResourceUtil;

public class HelpAction extends AbstractAction {
	private final PvpContext context;

	public HelpAction(PvpContext contextParam) {
		super(null, ResourceUtil.getIcon("help"));
		context = contextParam;
	}

	public void actionPerformed(ActionEvent e) {
		context.uiMain.showTab(OtherTab.Help);
	}
}
