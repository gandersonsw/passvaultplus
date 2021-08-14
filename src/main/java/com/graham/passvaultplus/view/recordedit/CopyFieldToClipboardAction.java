/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class CopyFieldToClipboardAction extends AbstractAction {

	private final RecordEditFieldText ref;

	CopyFieldToClipboardAction(ImageIcon icon, final RecordEditFieldText refParam) {
		super(null, icon);
		ref = refParam;
	
	}
	public void actionPerformed(ActionEvent e) {
		String txt = ref.getFieldTextForCopy();
		if (txt != null) {
			if (txt.length() > 0) {
				StringSelection ss = new StringSelection(txt);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
			}
		}
	}
	
}