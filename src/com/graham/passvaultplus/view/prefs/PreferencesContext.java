/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.graham.passvaultplus.model.core.PvpFileInterface;

public class PreferencesContext {
	final boolean compressedFlag; // this is not updated, original value only
	final boolean encryptedFlag;  // this is not updated, original value only
	
	JCheckBox compressed;
	JCheckBox encrypted;
	JCheckBox savePassword;
	JCheckBox showPassword;
	JPasswordField password;
	JTextField passwordClearText;
	JLabel errorMessage; // TODO probably can delete this 
	ConfigAction configAction;
	JComboBox<ConfigAction> actionCombo;
	JComboBox<String> aesBits;
	JButton saveButton;
	JLabel passwordStrength;
	
	private JLabel dataFileLabel;
	private String dataFileString;
	private File dataFile;
	
	public PreferencesContext(final PreferencesConnection connParam) {
		dataFileString = connParam.getDataFilePath();
		compressedFlag = PvpFileInterface.isCompressed(dataFileString);
		encryptedFlag = PvpFileInterface.isEncrypted(dataFileString);
	}
	
	public String getPasswordText() {
		if (showPassword.isSelected()) {
			return passwordClearText.getText();
		} else {
			return new String(password.getPassword());
		}
	}

	public File getDataFile() {
		if (dataFile == null) {
			dataFile = new File(dataFileString);
		}
		return dataFile;
	}
	
	public String getDataFileString() {
		return dataFileString;
	}
	
	public void setDataFile(final File f, final int aesBits) {
		if (f == null) {
			dataFile = null;
			dataFileString = "";
		} else {
			String fname = f.getName();
			boolean isCompressed = PvpFileInterface.isCompressed(fname);
			boolean isEncrypted = PvpFileInterface.isEncrypted(fname);
			fname = PvpFileInterface.formatFileName(fname, isCompressed, isEncrypted);
			dataFile = new File(f.getParentFile(), fname);
			dataFileString = dataFile.getAbsolutePath();
			compressed.setSelected(isCompressed);
			encrypted.setSelected(isEncrypted);
			setSelectedBits(aesBits);
		}
		
		dataFileLabel.setText(dataFileString);
	}
	
	public void setSelectedBits(final int bits) {
		if (bits == 0) {
			// if its 0, dont change it
		}else if (bits == 256) {
			aesBits.setSelectedIndex(2);
		} else if (bits == 192) {
			aesBits.setSelectedIndex(1);
		} else {
			aesBits.setSelectedIndex(0);
		}
	}
	
	public void setDataFileLabel(final JLabel l) {
		dataFileLabel = l;
	}
	
	public Action getDefaultFileAction() {
		return new SetDefaultDataFile(getDataFile());
	}
	
	public void setFileExtensionFromCompressedAndEncrypted() {
		if (dataFile != null) {
			String fname = PvpFileInterface.formatFileName(dataFile.getName(), compressed.isSelected(), encrypted.isSelected());
			dataFile = new File(dataFile.getParentFile(), fname);
			dataFileString = dataFile.getAbsolutePath();
			dataFileLabel.setText(dataFileString);
		}
	}
	
	class SetDefaultDataFile extends AbstractAction {
		final private File defaultFile;
		public SetDefaultDataFile(final File f) {
			super("Default");
			defaultFile = f;
		}
		public void actionPerformed(ActionEvent e) {
			setDataFile(defaultFile, 0); // TODO test this 0
		}
	}

}
