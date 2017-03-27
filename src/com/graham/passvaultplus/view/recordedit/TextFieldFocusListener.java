/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JFrame;
import javax.swing.JTextField;

import com.graham.passvaultplus.model.core.PvpField;
import com.graham.swingui.datepicker.DatePicker;

public class TextFieldFocusListener implements FocusListener {

	private final PvpField field;
	private final JFrame mainFrame;
	private final JTextField textField;
	
	private DatePicker dateP;
	
	public TextFieldFocusListener(final JFrame mf, final JTextField tf, PvpField f) {
		field = f;
		mainFrame = mf;
		textField = tf;
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (dateP != null) {
			System.out.println("already there");
			return;
		}
		
		if (field == null || field.getType() == null) {
			return;
		}
		
		if (field.getType().equals(PvpField.TYPE_DATE)) {
			dateP = new DatePicker(mainFrame, textField, false);
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (dateP != null) {
			dateP.close();
			dateP = null;
		}
	}
}
