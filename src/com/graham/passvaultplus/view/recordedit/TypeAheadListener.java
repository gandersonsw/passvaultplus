/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class TypeAheadListener extends AbstractAction {
	final private JComboBox cb;
	final private JTextField tf;

	public TypeAheadListener(JComboBox cbParam, JTextField tfParam) {
		cb = cbParam;
		tf = tfParam;
	}
	public void actionPerformed(ActionEvent e) {
		tf.setText((String)cb.getSelectedItem());
	}
}