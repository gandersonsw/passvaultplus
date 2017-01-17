/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;

public class CopyFieldToClipboardAction extends AbstractAction {
	private JTextComponent tc;
	private String overrideText;

	public CopyFieldToClipboardAction(ImageIcon icon, JTextComponent tcParam) {
		super(null, icon);
		tc = tcParam;
	}
	public void actionPerformed(ActionEvent e) {
		String txt = overrideText == null ? tc.getText() : overrideText;
		if (txt != null) {
			if (txt.length() > 0) {
				StringSelection ss = new StringSelection(txt);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
			}
		}
	}
	public void setOverrideText(final String s) {
		overrideText = s;
	}
}