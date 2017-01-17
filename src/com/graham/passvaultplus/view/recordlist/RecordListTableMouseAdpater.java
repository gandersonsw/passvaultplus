/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordlist;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;

public class RecordListTableMouseAdpater extends MouseAdapter {
	final AbstractAction action;
	
	public RecordListTableMouseAdpater(final AbstractAction actionParam) {
		action = actionParam;
	}

	public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
        	action.actionPerformed(null);
        }
    }

	
}


