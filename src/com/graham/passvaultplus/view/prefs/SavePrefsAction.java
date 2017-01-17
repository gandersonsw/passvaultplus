/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpFileInterface;

public class SavePrefsAction extends AbstractAction {
	final private PvpContext context;
	final private PreferencesContext prefsContext;

	public SavePrefsAction(final PvpContext contextParam, final PreferencesContext prefsContextParam) {
		super("Save");
		context = contextParam;
		prefsContext = prefsContextParam;
	}

	public void actionPerformed(ActionEvent e) {

		String newPassword = prefsContext.password.getText();
		String oldPassword = context.getPassword();

		if (newPassword.trim().length() == 0 && prefsContext.encrypted.isSelected()) {
			prefsContext.errorMessage.setText("Password required when encrypted.");
			return;
		}

		if (doDirStuff()) {
			return;
		}

		boolean saveFlag = false;
		if (oldPassword == null) {
			oldPassword = "";
		}

		if (!prefsContext.savePassword.isSelected() && oldPassword.length() > 0) {
			// User does not want to save password anymore
			// 1. clear out saved password
			context.setPassword("", true);
			// 2. set up this value so the user is not asked for their password until next launch
			context.setPasswordFromUserForThisRuntime(oldPassword);
			saveFlag = true;
		} else if (!oldPassword.equals(newPassword)) {
			if (prefsContext.savePassword.isSelected()) {
				context.setPassword(newPassword, true);
			} else {
				context.setPassword("", true);
				context.setPasswordFromUserForThisRuntime(newPassword);
			}
			saveFlag = true;
		}

		if (prefsContext.compressed.isSelected() != prefsContext.compressedFlag || prefsContext.encrypted.isSelected() != prefsContext.encryptedFlag) {
			String path = BCUtil.getFileNameNoExt(context.getDataFilePath(), true);
			if (prefsContext.compressed.isSelected()) {
				path = path + "." + PvpFileInterface.EXT_COMPRESS;
			}
			if (prefsContext.encrypted.isSelected()) {
				path = path + "." +  PvpFileInterface.EXT_ENCRYPT;
			}
			if (!prefsContext.encrypted.isSelected() && !prefsContext.compressed.isSelected()) {
				path = path + "." + PvpFileInterface.EXT_XML;
			}
			context.setDataFilePath(path);
			saveFlag = true;
		}

		if (saveFlag) {
			context.getFileInterface().save(context.getDataInterface());
		}

		context.getTabManager().removeOtherTab(context.getPrefsComponent());
		context.setPrefsComponent(null);
	}

	/**
	 * @return true if there was an error, and should stop the save process.
	 */
	private boolean doDirStuff() {
		// TODO marker101 dup code
		String path = prefsContext.dir.getText();
		File f = new File(path);
		//if (path.equals(defaultPath)) {
		//	f.mkdirs();
		//} else {
			if (!f.isFile()) {
				// TODO not sure this is right
				JOptionPane.showMessageDialog(context.getMainFrame(),"That file does not exist on the file system. Please create it or use a different path.");
				return true;
			}
		//}
		context.setDataFilePath(path);
		//setVisible(false);
		//if (appFirstStarting) {
		//	context.dataFileSelectedForStartup();
		//}
		return false;
	}

}
