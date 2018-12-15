/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.MyCipherFactory;
import com.graham.passvaultplus.model.core.PvpBackingStoreOtherFile;
import com.graham.passvaultplus.model.core.PvpOutStreamer;
import com.graham.passvaultplus.view.JceDialog;

public class SavePrefsAction extends AbstractAction {

	final private PreferencesConnection conn;
	final private PreferencesContext prefsContext;

	public SavePrefsAction(final PreferencesContext prefsContextParam) {
		super(prefsContextParam.configAction.getButtonLabel());
		conn = prefsContextParam.conn;
		prefsContext = prefsContextParam;
	}

	public void actionPerformed(ActionEvent e) {
		boolean completed = false;
		if (prefsContext.configAction == ConfigAction.Create) {
			completed = doCreate();
		} else if (prefsContext.configAction == ConfigAction.Open) {
			completed = doOpen();
		} else if (prefsContext.configAction == ConfigAction.Change) {
			completed = doChange();
		} else {
			throw new RuntimeException("unexpected action: " + prefsContext.configAction);
		}
		if (completed) {
			prefsContext.remoteBS.cleanup();
		}
	}

	private boolean doCreate() {
		final String newPassword = prefsContext.getPasswordText();

		final File dataFile = prefsContext.getDataFile();
		if (dataFile.isFile()) {
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "When creating a new database, a file must not exist in the choosen location. \nPlease choose a directory where there is no file, or use the \"Open Existing Database\" option.");
			return false;
		}

		setContextPrefsValues();
		if (!validatePin()) {
			return false;
		}

		if (prefsContext.encrypted.isSelected()) {
			if (newPassword.trim().length() == 0) {
				JOptionPane.showMessageDialog(conn.getSuperFrame(), "Password required when encrypted.");
				return false;
			}
			if (checkEncryptionStrength()) {
				return false;
			}
		}

		if (conn.isDefaultPath(dataFile.getAbsolutePath())) {
			boolean mkret = new File(dataFile.getParent()).mkdirs();
			if (!mkret) {
				JOptionPane.showMessageDialog(conn.getSuperFrame(), "There was a problem creating the directory:" + dataFile.getParent());
			}
		}

		try {
			createDefaultStarterFile();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "Could not create default file: " + ex.getMessage());
			return false;
		}

		return conn.doOpen();
	}

	private boolean doOpen() {
		final String newPassword = prefsContext.getPasswordText();

		if (newPassword.trim().length() == 0 && prefsContext.encrypted.isSelected()) {
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "Password required when encrypted.");
			return false;
		}

		if (!validatePin()) {
			return false;
		}

		final File dataFile = prefsContext.getDataFile();
		if (!dataFile.isFile()) {
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "That file does not exist on the file system. Please create it or use a different path.");
			return false;
		}

		setContextPrefsValues();
		return conn.doOpen();
	}

	private boolean doChange() {
		final String newPassword = prefsContext.getPasswordText();
		boolean saveFlag = false;
		setContextPrefsValues();

		if (prefsContext.encrypted.isSelected()) {
			if (newPassword.trim().length() == 0) {
				JOptionPane.showMessageDialog(conn.getSuperFrame(), "Password required when encrypted.");
				return false;
			}
			String oldPassword = conn.getContextPrefs().getPassword();
			if (oldPassword == null) {
				oldPassword = "";
			}
			if (!oldPassword.equals(newPassword)) {
				saveFlag = true;
			}
			if (conn.getContextPrefsOriginal().getEncryptionStrengthBits() != conn.getContextPrefs().getEncryptionStrengthBits()) {
				saveFlag = true;
			}
			if (checkEncryptionStrength()) {
				return false;
			}
		}

		if (!validatePin()) {
			return false;
		}

		if (!conn.isDefaultPath(prefsContext.getDataFile().getAbsolutePath())) {
			saveFlag = true;
		}

		if (prefsContext.compressed.isSelected() != prefsContext.oCompressedFlag || prefsContext.encrypted.isSelected() != prefsContext.oEncryptedFlag) {
			saveFlag = true;
		}

		if (prefsContext.remoteBS.shouldSaveOnChange()) {
			saveFlag = true;
		}

		if (!prefsContext.remoteBS.presave()) {
			return false;
		}
		return conn.doSave(saveFlag);
	}

	private boolean validatePin() {
		if (prefsContext.usePin.isSelected()) {
			final String pin = prefsContext.getPinText();
			if (pin.length() == 0) {
				JOptionPane.showMessageDialog(conn.getSuperFrame(), "Pin must be at least 1 character long.");
				return false;
			}
			if (pin.length() > 200) {
				JOptionPane.showMessageDialog(conn.getSuperFrame(), "Pin cant be longer than 200 characters.");
				return false;
			}
		}
		return true;
	}

	private void setContextPrefsValues() {
		conn.getContextPrefs().setPasswordAndPin(prefsContext.getPasswordText(), prefsContext.savePassword.isSelected(), prefsContext.getPinText(), prefsContext.usePin.isSelected());
		int encryptBits = 0;
		if (this.prefsContext.encrypted.isSelected()) {
			try { encryptBits = Integer.parseInt(prefsContext.aesBits.getSelectedItem().toString()); } catch (Exception e) { encryptBits = 128; }
		}
		conn.getContextPrefs().setDataFilePath(prefsContext.getDataFile().getAbsolutePath(), encryptBits);
		int pinTimeout = 0;
		int pinMaxTry = 100;
		if (this.prefsContext.encrypted.isSelected()) {
			try { pinTimeout = Integer.parseInt(prefsContext.timeoutCombo.getSelectedItem().toString()); } catch (Exception e) { }
			try { pinMaxTry = Integer.parseInt(prefsContext.pinMaxTryCombo.getSelectedItem().toString()); } catch (Exception e) { }
		}
		conn.getContextPrefs().setPinTimeout(pinTimeout);
		conn.getContextPrefs().setPinMaxTry(pinMaxTry);
		conn.getContextPrefs().setShowDashboard(prefsContext.showDashboard.isSelected());
		conn.getContextPrefs().setUseGoogleDrive(prefsContext.remoteBS.useGoogleDrive.isSelected());
		conn.getContextPrefs().setShowDiagnostics(prefsContext.showDiagnostics.isSelected());
	}

	private boolean checkEncryptionStrength() {
		try {
			final int aesBits = conn.getContextPrefs().getEncryptionStrengthBits();
			final int maxKL = MyCipherFactory.getMaxAllowedAESKeyLength();
			if (maxKL < aesBits) {
				final JceDialog jced = new JceDialog();
				jced.showDialog(conn.getSuperFrame(), maxKL);
				return true;
			}
		} catch (NoSuchAlgorithmException e) {
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "Unexpected encryption error:" + e.getMessage());
			return true;
		}
		return false;
	}

	private void createDefaultStarterFile() throws Exception {
		InputStream sourceStream = null;
		InputStreamReader isr = null;
		BufferedReader bufR = null;
		PvpOutStreamer fileWriter = null;
		try {
			if (PvpContext.JAR_BUILD) {
				// note path starts with "/" - that starts at the root of the jar,
				// instead of the location of the class.
				sourceStream = PvpContext.class.getResourceAsStream("/starter-pvp-data.xml");
				isr = new InputStreamReader(sourceStream);
			} else {
				File sourceFile = new File("src/main/resources/starter-pvp-data.xml");
				isr = new FileReader(sourceFile);
			}

			bufR = new BufferedReader(isr);
			fileWriter = new PvpOutStreamer(new PvpBackingStoreOtherFile(conn.getContextPrefs().getDataFile()), conn.getContextPrefs());
			final BufferedWriter bw = fileWriter.getWriter();
			String line;
			while ((line = bufR.readLine()) != null) {
				bw.write(line);
				bw.newLine();
			}
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
			if (bufR != null) {
				try { bufR.close(); } catch (Exception e) { }
			}
			if (isr != null) {
				try { isr.close(); } catch (Exception e) { }
			}
			if (sourceStream != null) {
				try { sourceStream.close(); } catch (Exception e) { }
			}
		}
	}

}
