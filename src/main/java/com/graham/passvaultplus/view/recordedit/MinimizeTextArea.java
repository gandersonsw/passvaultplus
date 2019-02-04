/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;

public class MinimizeTextArea extends AbstractAction {

	final private RecordEditContext editContext;
	final private JTextArea ta;
	final private Container taParent;
	
	MinimizeTextArea(final ImageIcon icon, final RecordEditContext editContextParam, final JTextArea taParam, final Container taParentParam) {
		super(icon == null ? "m" : null, icon);
		editContext = editContextParam;
		ta = taParam;
		taParent = taParentParam;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0079");
		ta.getParent().remove(ta);
		taParent.add(ta);
		editContext.panelInTabPane.remove(editContext.maximizedTextAreaPanel);
		editContext.panelInTabPane.add(editContext.centerPaneWithFields, BorderLayout.CENTER);
		editContext.panelInTabPane.revalidate();
		editContext.panelInTabPane.repaint();
	}

}
