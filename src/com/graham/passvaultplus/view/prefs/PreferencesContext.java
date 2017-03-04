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
	final PreferencesConnection conn;
	
	JCheckBox compressed;
	JCheckBox encrypted;
	JCheckBox savePassword;
	JCheckBox showPassword;
	JPasswordField password;
	JTextField passwordClearText;
	
	JPasswordField pin;
	JTextField pinClearText;
	JCheckBox showPin;
	JCheckBox usePin;
	JComboBox<String> timeoutCombo;
	
	ConfigAction configAction;
	JComboBox<ConfigAction> actionCombo;
	JComboBox<String> aesBits;
	JButton saveButton;
	JLabel passwordStrength;
	
	private JLabel dataFileLabel;
	private String dataFileString;
	private File dataFile;
	
	PreferencesContext(final PreferencesConnection connParam) {
		conn = connParam;
		dataFileString = connParam.getDataFilePath();
		compressedFlag = PvpFileInterface.isCompressed(dataFileString);
		encryptedFlag = PvpFileInterface.isEncrypted(dataFileString);
	}
	
	String getPasswordText() {
		if (showPassword.isSelected()) {
			return passwordClearText.getText();
		} else {
			return new String(password.getPassword());
		}
	}
	
	String getPinText() {
		if (showPin.isSelected()) {
			return pinClearText.getText();
		} else {
			return new String(pin.getPassword());
		}
	}

	File getDataFile() {
		if (dataFile == null) {
			dataFile = new File(dataFileString);
		}
		return dataFile;
	}
	
	String getDataFileString() {
		return dataFileString;
	}
	
	void setDataFile(final File f, final int aesBits) {
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
	
	void setSelectedBits(final int bits) {
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
	
	void setPinTimeout(final int to) {
		if (to < 2 || to > 300) {
			timeoutCombo.setSelectedItem("Never");
		} else {
			timeoutCombo.setSelectedItem(Integer.toString(to));
		}
	}
	
	void setDataFileLabel(final JLabel l) {
		dataFileLabel = l;
	}
	
	Action getDefaultFileAction() {
		return new SetDefaultDataFile(getDataFile());
	}
	
	/**
	 * Call when the compressed checkbox or encrypted checkbox changed
	 */
	void updateBecauseCompressedOrEncryptedChanged() {
		if (dataFile != null) {
			final String fname = PvpFileInterface.formatFileName(dataFile.getName(), compressed.isSelected(), encrypted.isSelected());
			dataFile = new File(dataFile.getParentFile(), fname);
			dataFileString = dataFile.getAbsolutePath();
			dataFileLabel.setText(dataFileString);
		}
		
		setItemsDependentOnEncryptedEnabled();
	}
	
	void setItemsDependentOnEncryptedEnabled() {
		if (encrypted.isSelected()) {
			this.password.setEnabled(true);
			this.passwordClearText.setEnabled(true);
			this.savePassword.setEnabled(true);
			this.aesBits.setEnabled(true);
		} else {
			this.password.setEnabled(false);
			this.passwordClearText.setEnabled(false);
			this.savePassword.setSelected(false);
			this.savePassword.setEnabled(false);
			this.usePin.setSelected(false);
			this.aesBits.setEnabled(false);
		}
		
		setPinEnabled();
	}
	
	void setPinEnabled() {
		if (this.savePassword.isSelected()) {
			this.usePin.setEnabled(true);
			this.pin.setEnabled(true);
			this.pinClearText.setEnabled(true);
			this.timeoutCombo.setEnabled(true);
		} else {
			this.usePin.setSelected(false);
			this.usePin.setEnabled(false);
			this.pin.setEnabled(false);
			this.pinClearText.setEnabled(false);
			this.timeoutCombo.setEnabled(false);
		}
	}
	
	class SetDefaultDataFile extends AbstractAction {
		final private File defaultFile;
		public SetDefaultDataFile(final File f) {
			super("Default");
			defaultFile = f;
		}
		public void actionPerformed(ActionEvent e) {
			setDataFile(defaultFile, 0);
		}
	}

}
