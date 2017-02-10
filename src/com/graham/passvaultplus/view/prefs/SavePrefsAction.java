/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;

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
		
		if (newPassword.trim().length() == 0 && prefsContext.encrypted.isSelected()) {
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "Password required when encrypted.");
			//prefsContext.errorMessage.setText("Password required when encrypted.");// TODO delete this ?
			return;
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
			
		if (prefsContext.savePassword.isSelected()) {
			conn.setPassword(newPassword, true);
		}
		conn.setPasswordFromUserForThisRuntime(newPassword);
		
		conn.doOpen(prefsContext.getDataFile());
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
	
		if (prefsContext.savePassword.isSelected()) {
			conn.setPassword(newPassword, true);
		}
		conn.setPasswordFromUserForThisRuntime(newPassword);

		conn.doOpen(dataFile);
	}
	
	private void doChange() {
		final String newPassword = prefsContext.getPasswordText();
		boolean saveFlag = false;
		
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
		}
		
		if (!conn.isDefaultPath(prefsContext.getDataFile().getAbsolutePath())) {
			saveFlag = true;
		}
		
		if (prefsContext.savePassword.isSelected()) {
			conn.setPassword(newPassword, true);
		} else {
			conn.setPassword("", true);
		}
		conn.setPasswordFromUserForThisRuntime(newPassword);

		if (prefsContext.compressed.isSelected() != prefsContext.compressedFlag || prefsContext.encrypted.isSelected() != prefsContext.encryptedFlag) {
			saveFlag = true;
		}

		conn.doSave(prefsContext.getDataFile(), saveFlag);
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
