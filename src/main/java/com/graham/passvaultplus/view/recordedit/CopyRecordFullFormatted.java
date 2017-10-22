/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.model.core.PvpRecord;

public class CopyRecordFullFormatted extends AbstractAction {
	
	final PvpRecord r;
	
	public CopyRecordFullFormatted(final PvpRecord rParam) {
		super("Copy Formmatted");
		r = rParam;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final String text = r.getFormated();
		if (text != null) {
			if (text.length() > 0) {
				StringSelection ss = new StringSelection(text);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
			}
		}
	}

}
