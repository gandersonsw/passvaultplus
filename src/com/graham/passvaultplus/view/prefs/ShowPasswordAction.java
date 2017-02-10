/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ShowPasswordAction extends AbstractAction {
	
	final private PreferencesContext context;
	
	public ShowPasswordAction(final PreferencesContext contextParam) {
		context = contextParam;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (context.showPassword.isSelected()) {
			String pw = new String(context.password.getPassword());
			context.passwordClearText.setText(pw);
			context.password.setVisible(false);
			context.passwordClearText.setVisible(true);
			context.passwordClearText.getParent().revalidate();
		} else {
			String pw = context.passwordClearText.getText();
			context.password.setText(pw);
			context.password.setVisible(true);
			context.passwordClearText.setVisible(false);
			context.password.getParent().revalidate();
		}
	}

}
