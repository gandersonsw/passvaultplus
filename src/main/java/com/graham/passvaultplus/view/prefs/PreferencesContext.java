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

import com.graham.passvaultplus.model.core.PvpPersistenceInterface;

public class PreferencesContext {
	final boolean oCompressedFlag; // this is not updated, original value only
	final boolean oEncryptedFlag;  // this is not updated, original value only
	final PreferencesConnection conn;
	final RemoteBSPrefHandler remoteBS;

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
	JComboBox<String> pinMaxTryCombo;

	ConfigAction configAction;
	JComboBox<ConfigAction> actionCombo;
	JComboBox<String> aesBits;
	JButton saveButton;
	JLabel passwordStrength;

	JCheckBox showDashboard;
	JCheckBox showDiagnostics;

	private JLabel dataFileLabel;
	private String dataFileString;
	private File dataFile;

	public PreferencesContext(final PreferencesConnection connParam) {
		conn = connParam;
		dataFileString = connParam.getContextPrefs().getDataFilePath();
		oCompressedFlag = PvpPersistenceInterface.isCompressed(dataFileString);
		oEncryptedFlag = PvpPersistenceInterface.isEncrypted(dataFileString);
		remoteBS = new RemoteBSPrefHandler(this);
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
			boolean isCompressed = PvpPersistenceInterface.isCompressed(fname);
			boolean isEncrypted = PvpPersistenceInterface.isEncrypted(fname);
			fname = PvpPersistenceInterface.formatFileName(fname, isCompressed, isEncrypted);
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

	void setPinMaxTry(final int mt) {
		if (mt > 50) {
			pinMaxTryCombo.setSelectedItem("Unlimited");
		} else {
			pinMaxTryCombo.setSelectedItem(Integer.toString(mt));
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
			final String fname = PvpPersistenceInterface.formatFileName(dataFile.getName(), compressed.isSelected(), encrypted.isSelected());
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
		} else {
			this.usePin.setEnabled(false);
			this.usePin.setSelected(false);
		}
	}

	void setPinItemsEnabled() {
		if (this.usePin.isSelected()) {
			this.pin.setEnabled(true);
			this.pinClearText.setEnabled(true);
			this.timeoutCombo.setEnabled(true);
			this.pinMaxTryCombo.setEnabled(true);
		} else {
			this.pin.setEnabled(false);
			this.pinClearText.setEnabled(false);
			this.timeoutCombo.setEnabled(false);
			this.pinMaxTryCombo.setEnabled(false);
		}
	}

	public void cleanup() {
		remoteBS.cleanup();
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
