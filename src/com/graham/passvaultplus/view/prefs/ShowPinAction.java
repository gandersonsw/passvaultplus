/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ShowPinAction extends AbstractAction {
	final private PreferencesContext context;
	
	public ShowPinAction(final PreferencesContext contextParam) {
		context = contextParam;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (context.showPin.isSelected()) {
			final String pin = new String(context.pin.getPassword());
			context.pinClearText.setText(pin);
			context.pin.setVisible(false);
			context.pinClearText.setVisible(true);
			context.pinClearText.getParent().revalidate();
		} else {
			final String pw = context.pinClearText.getText();
			context.pin.setText(pw);
			context.pin.setVisible(true);
			context.pinClearText.setVisible(false);
			context.pin.getParent().revalidate();
		}
	}
}
