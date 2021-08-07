/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.model.core.PvpPersistenceInterface;
import com.graham.passvaultplus.PvpContextPrefs;

public class ConfigActionChanged extends AbstractAction {
	final private PreferencesContext context;

	public ConfigActionChanged(final PreferencesContext contextParam) {
		context = contextParam;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ConfigAction ca = (ConfigAction)context.actionCombo.getSelectedItem();

		if (ca == context.configAction) {
			// it didn't change, nothing to do
			return;
		}

		PvpContextPrefs contextPrefs = context.conn.getContextPrefs();
		context.saveButton.setText(ca.getButtonLabel());
		if (ca == ConfigAction.Create) {
			context.setDataFile(contextPrefs.getDataFile(), 0);
			context.encrypted.setEnabled(true);
			context.encrypted.setSelected(false);
			context.updateBecauseEncryptedChanged();
		} else if (ca == ConfigAction.Open) {
			context.encrypted.setEnabled(false);
			context.encrypted.setSelected(false);

			File f = context.getDataFile();
			if (f != null && f.isFile()) {
				// a file exists here
				final String fname = f.getName();
				context.encrypted.setSelected(PvpPersistenceInterface.isEncrypted(fname));
			} else {
				context.setDataFile(null, 0);
			}
			context.setItemsDependentOnEncryptedEnabled();
		} else if (ca == ConfigAction.Change) {
			context.setDataFile(new File(contextPrefs.getDataFilePath()), contextPrefs.getEncryptionStrengthBits());
			context.encrypted.setEnabled(true);
			context.encrypted.setSelected(context.oEncryptedFlag);
			context.password.setText(contextPrefs.getPassword());
			context.passwordClearText.setText(contextPrefs.getPassword());
			context.savePassword.setSelected(contextPrefs.isPasswordSaved());
			context.pin.setText(contextPrefs.getPin());
			context.pinClearText.setText(contextPrefs.getPin());
			context.usePin.setSelected(contextPrefs.getUsePin());
			context.setItemsDependentOnEncryptedEnabled();
		} else {
			throw new RuntimeException("unexpected action: " + ca);
		}

		context.configAction = ca;
	}

}
