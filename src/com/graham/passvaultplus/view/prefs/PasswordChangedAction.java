/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.framework.BCUtil;

public class PasswordChangedAction extends AbstractAction {
	
	final private PreferencesContext context;
	
	public PasswordChangedAction(final PreferencesContext contextParam) {
		context = contextParam;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final int bits = BCUtil.dataInString(context.getPasswordText());
		String rating;
		if (bits > 200) {
			rating = "Excellent * 2";
		} else if (bits > 150) {
			rating = "Excellent";
		} else if (bits > 100) {
			rating = "Good";
		} else if (bits > 60) { // TODO determine this number, ALL these numbers
			rating = "Acceptable";
		} else {
			rating = "Poor";
		}
		
		context.passwordStrength.setText(bits + " bits (" + rating + ")");
	}

}
