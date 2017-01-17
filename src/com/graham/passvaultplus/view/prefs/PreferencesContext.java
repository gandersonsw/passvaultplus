/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpFileInterface;

public class PreferencesContext {
	boolean compressedFlag;
	boolean encryptedFlag;
	JCheckBox compressed;
	JCheckBox encrypted;
	JCheckBox savePassword;
	JTextField password;
	JLabel errorMessage;
	JTextField dir;
	
	public PreferencesContext(final PvpContext contextParam) {
		final String path = contextParam.getDataFilePath();
		compressedFlag = PvpFileInterface.isCompressed(path);
		encryptedFlag = PvpFileInterface.isEncrypted(path);
		
		compressed = new JCheckBox("Compressed (zip)", compressedFlag);
		
		encrypted = new JCheckBox("Encrypted", encryptedFlag);
		
		savePassword = new JCheckBox("Save Password", contextParam.isPasswordSaved());
		savePassword.setToolTipText("If checked, the password will be saved. If not checked, you must enter the password when starting app.");
		
		if (contextParam.isPasswordSaved()) {
			password = new JTextField(contextParam.getPassword(), 20);
		} else {
			password = new JTextField(20);
		}
		
		errorMessage = new JLabel(" ");
	}
}
