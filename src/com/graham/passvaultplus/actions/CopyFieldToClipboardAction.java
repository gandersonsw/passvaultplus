/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;

public class CopyFieldToClipboardAction extends AbstractAction {
	private JTextComponent tc;

	public CopyFieldToClipboardAction(String label, ImageIcon icon, JTextComponent tcParam) {
		super(label, icon);
		tc = tcParam;
	}
	public void actionPerformed(ActionEvent e) {
		String txt = tc.getText();
		if (txt != null) {
			if (txt.length() > 0) {
				StringSelection ss = new StringSelection(txt);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
			}
		}
	}
}