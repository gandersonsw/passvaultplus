/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextField;

public class SetJTextFieldAction extends AbstractAction {
	final private JTextField tf;
	final private String text;

	public SetJTextFieldAction(final String label, final JTextField tfParam, final String textParam) {
		super(label);
		tf = tfParam;
		text = textParam;
	}

	public void actionPerformed(ActionEvent e) {
		tf.setText(text);
	}
}
