/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordlist;

import com.graham.passvaultplus.model.ListTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

public class RecordListCopyAction extends AbstractAction {
	JTable t;
	Action originalCopyAction;

	public RecordListCopyAction(JTable tparam) {
		t = tparam;
		originalCopyAction = t.getActionMap().get("copy");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (t.getSelectedRowCount() > 1) {
			originalCopyAction.actionPerformed(e);
		} else {
			ListTableModel tm = (ListTableModel)t.getModel();
			Object o = tm.getValueAt(t.getSelectedRow(), 1, true);
			if (o != null) {
				String s = o.toString();
				if (s.trim().length() > 0) {
					final StringSelection ss = new StringSelection(s);
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
				}
			}
		}
	}

}
