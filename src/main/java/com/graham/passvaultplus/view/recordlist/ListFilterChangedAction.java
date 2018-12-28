/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordlist;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.PvpContext;

public class ListFilterChangedAction extends AbstractAction {
	final private PvpContext context;
	
	public ListFilterChangedAction(final PvpContext c) {
		context = c;
	}

	public void actionPerformed(ActionEvent arg0) {
		context.uiMain.getViewListContext().filterUIChanged();
	}

}
