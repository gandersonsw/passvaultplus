/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;
import java.io.*;
import java.security.NoSuchAlgorithmException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.DatabaseReader;
import com.graham.passvaultplus.model.core.MyCipherFactory;
import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpPersistenceInterface;
import com.graham.passvaultplus.view.JceDialog;
import com.graham.passvaultplus.view.longtask.LongTask;
import com.graham.passvaultplus.view.longtask.LTManager;

public class SavePrefsAction extends AbstractAction {

	final private PreferencesConnection conn;
	final private PreferencesContext prefsContext;

	public SavePrefsAction(final PreferencesContext prefsContextParam) {
		super(prefsContextParam.configAction.getButtonLabel());
		conn = prefsContextParam.conn;
		prefsContext = prefsContextParam;
	}

	public void actionPerformed(ActionEvent e) {
		//boolean completed;
		if (prefsContext.configAction == ConfigAction.Create) {
			doCreate();
		} else if (prefsContext.configAction == ConfigAction.Open) {
			doOpen();
		} else if (prefsContext.configAction == ConfigAction.Change) {
			doChange();
		} else {
			throw new RuntimeException("unexpected action: " + prefsContext.configAction);
		}
		//if (completed) {
		//	prefsContext.remoteBS.cleanup();
		//}
	}

	private void doCreate() {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0047");
		final String newPassword = prefsContext.getPasswordText();

		final File dataFile = prefsContext.getDataFile();
		if (dataFile.isFile()) {
			JOptionPane.showMessageDialog(conn.getSuperFrame(), "When creating a new database, a file must not exist in the choosen location. \nPlease choose a directory where there is no file, or use the \"Open Existing Database\" option.");
			return;
		}

		setContextPrefsValues();
		if (!validatePin()) {
			return;
		}

		if (prefsContext.encrypted.isSelected()) {
			if (newPassword.trim().length() == 0) {
				JOptionPane.showMessageDialog(conn.getSuperFrame(), "Password required when encrypted.");
				return;
			}
			if (checkEncryptionStrength()) {
				return;
			}
		}

		if (conn.isDefaultPath(dataFile.getAbsolutePath())) {
			final File parDir = new File(dataFile.getParent());
			boolean mkret = parDir.mkdirs();
			if (!mkret && !parDir.isDirectory()) {
				JOptionPane.showMessageDialog(conn.getSuperFrame(), parDir.isDirectory() + ":There was a problem creating the directory:" + dataFile.getParent());
				return;
			}
		}

		// TODO Maybe start new thread here? instead of in ToLocalCopier
			LTManager.run(() -> {
					if (!prefsContext.remoteBS.presave(true)) {
							return;
					}
					try {
							createFiles();
					} catch (Exception ex) {
							// TODO need to make a method in PvpConextUI for this
							System.out.println("RemoteBSPrefHandler.doCreate.A");
							JOptionPane.showMessageDialog(conn.getSuperFrame(), "Could not create default file: " + ex.getMessage());
							return;
					}
					conn.doOpen(prefsContext);
			});
	}

	private void doOpen() {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0049");
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

			LTManager.run(() -> {
					if (!prefsContext.remoteBS.presave(false)) {
							return;
					}
					setContextPrefsValues();
					conn.doOpen(prefsContext);
			});
	}

	private void doChange() {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0051");
		final String newPassword = prefsContext.getPasswordText();
		boolean saveFlag = false;
		setContextPrefsValues();

		if (prefsContext.encrypted.isSelected()) {
			if (newPassword.trim().length() == 0) {
				JOptionPane.showMessageDialog(conn.getSuperFrame(), "Password required when encrypted.");
				return;
			}
			String oldPassword = conn.getContextPrefsOriginal().getPassword();
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
				return;
			}
		}

		if (!validatePin()) {
			return;
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

		final boolean saveFlagCopy = saveFlag;
	//		LTManager.run(() -> {
			LTManager.runWithProgress(() -> {
					if (prefsContext.remoteBS.presave(false)) {
							conn.doSave(saveFlagCopy, prefsContext);
					}
			}, "Updating Settings");
	}

	private boolean validatePin() {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0066");
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
		prefsContext.remoteBS.save();
		conn.getContextPrefs().setShowDiagnostics(prefsContext.showDiagnostics.isSelected());
	}

	private boolean checkEncryptionStrength() {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0067");
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

	private void createFiles() throws Exception {
		if (prefsContext.remoteBS.createFiles()) {
			// the file was copied from a remote - no need to create a default
			return;
		}
		InputStream sourceStream = null;
		try {
			if (PvpContext.JAR_BUILD) {
				// note path starts with "/" - that starts at the root of the jar,
				// instead of the location of the class.
				sourceStream = PvpContext.class.getResourceAsStream("/starter-pvp-data.xml");
			} else {
				File sourceFile = new File("src/main/resources/starter-pvp-data.xml");
				sourceStream = new FileInputStream(sourceFile);
			}
			PvpContext tempContext = new PvpContext(prefsContext.conn.getPvpContextOriginal(), prefsContext.conn.getContextPrefs()); // TODO marker901
			PvpDataInterface newDataInterface = DatabaseReader.read(tempContext, sourceStream);
			PvpPersistenceInterface pi = new PvpPersistenceInterface(tempContext);
			pi.save(newDataInterface, PvpPersistenceInterface.SaveTrigger.init); // TODO test these lines


			//BCUtil.copyFile(sourceStream, conn.getContextPrefs().getDataFile());
		} finally {
			if (sourceStream != null) {
				try { sourceStream.close(); } catch (Exception e) { }
			}
		}
	}

}
