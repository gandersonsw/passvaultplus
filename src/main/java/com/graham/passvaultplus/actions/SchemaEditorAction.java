/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.view.OtherTab;
import com.graham.util.ResourceUtil;

public class SchemaEditorAction extends AbstractAction {
	final private PvpContext context;

	public SchemaEditorAction(final PvpContext contextParam) {
		super(null, ResourceUtil.getIcon("schema"));
		context = contextParam;
	}

	public void actionPerformed(ActionEvent e) {
		context.uiMain.showTab(OtherTab.SchemaEdit);
	}
}
