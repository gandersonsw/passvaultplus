/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TextFieldChangeForwarder implements DocumentListener {
	final private ActionListener actionLis;
	
	public TextFieldChangeForwarder(final ActionListener al) {
		actionLis = al;
	}

	public void changedUpdate(DocumentEvent arg0) {
		ActionEvent ae = new ActionEvent(arg0.getDocument(), 0, "change");
		actionLis.actionPerformed(ae);
	}

	public void insertUpdate(DocumentEvent arg0) {
		ActionEvent ae = new ActionEvent(arg0.getDocument(), 0, "change");
		actionLis.actionPerformed(ae);
	}

	public void removeUpdate(DocumentEvent arg0) {
		ActionEvent ae = new ActionEvent(arg0.getDocument(), 0, "change");
		actionLis.actionPerformed(ae);
	}

}
