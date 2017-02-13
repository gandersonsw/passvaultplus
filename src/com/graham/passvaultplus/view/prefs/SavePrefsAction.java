/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.MyCipherFactory;

public class SavePrefsAction extends AbstractAction {
	
	final private PreferencesConnection conn;
	final private PreferencesContext prefsContext;

	public SavePrefsAction(final PreferencesConnection connParam, final PreferencesContext prefsContextParam) {
		super(prefsContextParam.configAction.getButtonLabel());
		conn = connParam;
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
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "When creating a new database, a file must not exist in the choosen location. Please choose a directory where there is no file, or use the \"Open Existing Database\" option.");
			return;
		}
		
		final PrefsSettingsParam psp = createPspFromContext();
		
		if (prefsContext.encrypted.isSelected()) {
			if (newPassword.trim().length() == 0) {
				JOptionPane.showMessageDialog(conn.getSuperFrame(), "Password required when encrypted.");
				//prefsContext.errorMessage.setText("Password required when encrypted.");// TODO delete this ?
				return;
			}
			if (checkEncryptionStrength(psp.aesBits)) {
				return;
			}
		}
		
		if (conn.isDefaultPath(dataFile.getAbsolutePath())) {
			boolean mkret = new File(dataFile.getParent()).mkdirs();
			System.out.println("mkret: " + mkret);
		}
		
		try {
			createDefaultStarterFile(dataFile); // TODO need to zip/AES - move this to PvpFileReader
		} catch (Exception ex) {
			throw new RuntimeException(ex); // TODO handle some errors better
		}
		
		conn.doOpen(psp);
	}
	
	private void doOpen() {
		final String newPassword = prefsContext.getPasswordText();

		if (newPassword.trim().length() == 0 && prefsContext.encrypted.isSelected()) {
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "Password required when encrypted.");
			//prefsContext.errorMessage.setText("Password required when encrypted."); // TODO delete this
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
				//prefsContext.errorMessage.setText("Password required when encrypted."); // TODO delete this
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
		
		if (!conn.isDefaultPath(prefsContext.getDataFile().getAbsolutePath())) {
			saveFlag = true;
		}

		if (prefsContext.compressed.isSelected() != prefsContext.compressedFlag || prefsContext.encrypted.isSelected() != prefsContext.encryptedFlag) {
			saveFlag = true;
		}
		
		conn.doSave(psp, saveFlag);
	}
	
	private PrefsSettingsParam createPspFromContext() {
		final PrefsSettingsParam psp = new PrefsSettingsParam();
		psp.pw = prefsContext.getPasswordText();
		psp.f = prefsContext.getDataFile();
		psp.spw = prefsContext.savePassword.isSelected();
		try { psp.aesBits = Integer.parseInt(prefsContext.aesBits.getSelectedItem().toString()); } catch (Exception e) { psp.aesBits = 128; }
		return psp;
	}
	
	private boolean checkEncryptionStrength(final int aesBits) {
		try {
			final int maxKL = MyCipherFactory.getMaxAllowedAESKeyLength();
			if (maxKL < aesBits) {
				JOptionPane.showMessageDialog(conn.getSuperFrame(), "Strong AES is not enabled on this computer TODO.  Maximum key size:" + maxKL + " bits"); // TODO make a link with instractions how to do this
				return true;
			}
		} catch (NoSuchAlgorithmException e) {
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "Unexpected encryption error:" + e.getMessage());
			return true;
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return false;
	}
	
	private void createDefaultStarterFile(final File destinationFile) throws IOException {
		if (PvpContext.JAR_BUILD) {
			// note path starts with "/" - that starts at the root of the jar,
			// instead of the location of the class.
			InputStream sourceStream = PvpContext.class.getResourceAsStream("/datafiles/starter-pvp-data.xml");
			BCUtil.copyFile(sourceStream, destinationFile);
		} else {
			File sourceFile = new File("datafiles/starter-pvp-data.xml");
			System.out.println("sourceFile=" + sourceFile.getAbsolutePath());
			BCUtil.copyFile(sourceFile, destinationFile);
		}
	}

}
