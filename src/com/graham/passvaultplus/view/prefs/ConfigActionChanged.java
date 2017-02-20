/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

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
		
		context.saveButton.setText(ca.getButtonLabel());
		if (ca == ConfigAction.Create) {
			context.setDataFile(new File(context.conn.getDataFilePath()), 0);
			context.compressed.setEnabled(true);
			context.compressed.setSelected(false);
			context.encrypted.setEnabled(true);
			context.encrypted.setSelected(false);
			//context.password.setText("");
			context.aesBits.setEnabled(true);
		} else if (ca == ConfigAction.Open) {
			context.compressed.setEnabled(false);
			context.compressed.setSelected(false);
			context.encrypted.setEnabled(false);
			context.encrypted.setSelected(false);
			//context.password.setText("");
			context.aesBits.setEnabled(false);
			
			File f = context.getDataFile();
			if (f != null && f.isFile()) {
				// a file exists here,
				// context.setCompressAndEncryptFromFile(f);
			} else {
				context.setDataFile(null, 0);
			}
		} else if (ca == ConfigAction.Change) {
			context.setDataFile(new File(context.conn.getDataFilePath()), context.conn.getAesBits());
			context.compressed.setEnabled(true);
			context.compressed.setSelected(context.compressedFlag);
			context.encrypted.setEnabled(true);
			context.encrypted.setSelected(context.compressedFlag);
			//context.password.setText("");
		} else {
			throw new RuntimeException("unexpected action: " + ca);
		}
		
		context.configAction = ca;
	}

}
