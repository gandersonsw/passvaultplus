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
import com.graham.passvaultplus.model.core.PvpFileWriter;
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
		if (prefsContext.configAction == ConfigAction.Create) {
			doCreate();
		} else if (prefsContext.configAction == ConfigAction.Open) {
			doOpen();
		} else if (prefsContext.configAction == ConfigAction.Change) {
			doChange();
		} else {
			throw new RuntimeException("unexpected action: " + prefsContext.configAction);
		}
	}

	private void doCreate() {
		final String newPassword = prefsContext.getPasswordText();

		final File dataFile = prefsContext.getDataFile();
		if (dataFile.isFile()) {
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "When creating a new database, a file must not exist in the choosen location. \nPlease choose a directory where there is no file, or use the \"Open Existing Database\" option.");
			return;
		}
		
		final PrefsSettingsParam psp = createPspFromContext();
		if (!validatePin()) {
			return;
		}
	
		if (prefsContext.encrypted.isSelected()) {
			if (newPassword.trim().length() == 0) {
				JOptionPane.showMessageDialog(conn.getSuperFrame(), "Password required when encrypted.");
				return;
			}
			if (checkEncryptionStrength(psp.aesBits)) {
				return;
			}
		}
		
		if (conn.isDefaultPath(dataFile.getAbsolutePath())) {
			boolean mkret = new File(dataFile.getParent()).mkdirs();
			if (!mkret) {
				JOptionPane.showMessageDialog(conn.getSuperFrame(), "There was a problem creating the directory:" + dataFile.getParent());
			}
		}
		
		try {
			createDefaultStarterFile(psp);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "Could not create default file: " + ex.getMessage());
			return;
		}
		
		conn.doOpen(psp);
	}
	
	private void doOpen() {
		final String newPassword = prefsContext.getPasswordText();

		if (newPassword.trim().length() == 0 && prefsContext.encrypted.isSelected()) {
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "Password required when encrypted.");
			return;
		}
		
		if (!validatePin()) {
			return;
		}

		final File dataFile = prefsContext.getDataFile();
		if (!dataFile.isFile()) {
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "That file does not exist on the file system. Please create it or use a different path.");
			return;
		}
	
		final PrefsSettingsParam psp = createPspFromContext();
		conn.doOpen(psp);
	}
	
	private void doChange() {
		final String newPassword = prefsContext.getPasswordText();
		boolean saveFlag = false;
		final PrefsSettingsParam psp = createPspFromContext();
		
		if (prefsContext.encrypted.isSelected()) {
			if (newPassword.trim().length() == 0) {
				JOptionPane.showMessageDialog(conn.getSuperFrame(), "Password required when encrypted.");
				return;
			}
			String oldPassword = conn.getPassword();
			if (oldPassword == null) {
				oldPassword = "";
			}
			if (!oldPassword.equals(newPassword)) {
				saveFlag = true;
			}
			if (psp.aesBits != conn.getAesBits()) {
				saveFlag = true;
			}
			if (checkEncryptionStrength(psp.aesBits)) {
				return;
			}
		}
		
		if (!validatePin()) {
			return;
		}
		
		if (!conn.isDefaultPath(prefsContext.getDataFile().getAbsolutePath())) {
			saveFlag = true;
		}

		if (prefsContext.compressed.isSelected() != prefsContext.compressedFlag || prefsContext.encrypted.isSelected() != prefsContext.encryptedFlag) {
			saveFlag = true;
		}
		
		conn.doSave(psp, saveFlag);
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
	
	private PrefsSettingsParam createPspFromContext() {
		final PrefsSettingsParam psp = new PrefsSettingsParam();
		psp.pw = prefsContext.getPasswordText();
		psp.f = prefsContext.getDataFile();
		psp.spw = prefsContext.savePassword.isSelected();
		if (this.prefsContext.encrypted.isSelected()) {
			try { psp.aesBits = Integer.parseInt(prefsContext.aesBits.getSelectedItem().toString()); } catch (Exception e) { psp.aesBits = 128; }
		} else {
			psp.aesBits = 0;
		}
		psp.pin = prefsContext.getPinText();
		psp.usePin = prefsContext.usePin.isSelected();
		if (this.prefsContext.encrypted.isSelected()) {
			try { psp.pinTimeout = Integer.parseInt(prefsContext.timeoutCombo.getSelectedItem().toString()); } catch (Exception e) { psp.pinTimeout = 0; }
		} else {
			psp.pinTimeout = 0;
		}	
		psp.showDashBoard = prefsContext.showDashboard.isSelected();
		return psp;
	}
	
	private boolean checkEncryptionStrength(final int aesBits) {
		try {
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
	
	private void createDefaultStarterFile(final PrefsSettingsParam psp) throws Exception {
		InputStream sourceStream = null;
		InputStreamReader isr = null;
		BufferedReader bufR = null;
		PvpFileWriter fileWriter = null;
		try {
			if (PvpContext.JAR_BUILD) {
				// note path starts with "/" - that starts at the root of the jar,
				// instead of the location of the class.
				sourceStream = PvpContext.class.getResourceAsStream("/resources/starter-pvp-data.xml");
				isr = new InputStreamReader(sourceStream);
			} else {
				File sourceFile = new File("src/main/resources/starter-pvp-data.xml");
				isr = new FileReader(sourceFile);
			}
			
			bufR = new BufferedReader(isr);
			fileWriter = new PvpFileWriter(psp.f, psp.pw, psp.aesBits);
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
