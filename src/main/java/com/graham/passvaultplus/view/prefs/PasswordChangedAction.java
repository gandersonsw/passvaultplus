/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.util.StringUtil;

public class PasswordChangedAction extends AbstractAction {

	final private PreferencesContext context;

	public PasswordChangedAction(final PreferencesContext contextParam) {
		context = contextParam;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final int bits = StringUtil.dataInString(context.getPasswordText());
		String rating;
		if (bits > 128) {
			rating = "Excellent";
		} else if (bits > 64) {
			rating = "Good";
		} else if (bits > 44) {
			rating = "Average";
		} else {
			rating = "Weak";
		}

		context.passwordStrength.setText(bits + " bits (" + rating + ")");
	}

}
