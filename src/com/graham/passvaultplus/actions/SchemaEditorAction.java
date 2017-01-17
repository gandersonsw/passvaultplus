/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.PvpContext;

public class SchemaEditorAction extends AbstractAction {

	public SchemaEditorAction() {
		super(null, PvpContext.getIcon("schema"));
	}

	public void actionPerformed(ActionEvent e) {

		System.out.println("do schema editor"); // TODO

	}
}
