/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.view.schemaedit.SchemaEditBuilder;

public class SchemaEditorAction extends AbstractAction {
	final private PvpContext context;

	public SchemaEditorAction(final PvpContext contextParam) {
		super(null, PvpContext.getIcon("schema"));
		context = contextParam;
	}

	public void actionPerformed(ActionEvent e) {
		Component c = context.getSchemaEditComponent();
		if (c == null) {
			c = SchemaEditBuilder.buildEditor(context);
			context.setSchemaEditComponent(c);
			context.getTabManager().addOtherTab("Schema", c);
		}
		context.getTabManager().setSelectedComponent(c);
	}
}
