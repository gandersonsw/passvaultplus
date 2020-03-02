/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.dashboard;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpRecord;

public class UpcomingDatesTableMouseAdpater extends MouseAdapter {
	final private PvpContext context;
	final private JTable table;
	
	public UpcomingDatesTableMouseAdpater(final PvpContext contextParam, JTable t) {
		context = contextParam;
		table = t;
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) { // a double-click
			int row = table.getSelectedRow();
			if (row < 0) {
				return;
			}
			UpcomingDatesTableModel tm = (UpcomingDatesTableModel)table.getModel();
			PvpRecord r = tm.getRecordAtRow(row);
			context.uiMain.addRecordEditorIfNeeded(r);
		}
	}

}
