/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpPersistenceInterface;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;

/**
 * When the preferences are displayed as a tab in the main panel.
 */
public class PreferencesConnectionTab extends PreferencesConnection {

	public PreferencesConnectionTab(final PvpContext contextParam) {
		super(contextParam);
	}

	@Override
	public boolean doSave(final PrefsSettingsParam psp, final boolean wasChanges) {
		setContextFromPsp(psp);
		// if changes made don't require file rewrite, don't do it
		if (wasChanges) {
			//context.setDataFilePath(dataFile.getAbsolutePath());
			context.data.getFileInterface().save(context.data.getDataInterface(), PvpPersistenceInterface.SaveTrigger.major); // TODO if there is an exception here the changes from line 27 should not be applied
		}

		context.ui.getTabManager().removeOtherTab(context.ui.getPrefsComponent());
		context.ui.setPrefsComponent(null);
		return true;
	}

	@Override
	public boolean doOpen(final PrefsSettingsParam psp) {
		if (hasUnsavedChanges()) {
			int v = JOptionPane.showConfirmDialog(context.ui.getMainFrame(), "There are some records that have been edited but not saved. Are you sure you want to discard them?", "Unsaved changes", JOptionPane.OK_CANCEL_OPTION);
			if (v == JOptionPane.CANCEL_OPTION) {
				return false;
			}
		}

		// for now, dont support multiple databases open at once
		context.ui.getMainFrame().setVisible(false);
		setContextFromPsp(psp);

		PvpContext.startApp(false, psp.pw);
		return true;
	}

	private boolean hasUnsavedChanges() {
		for (RecordEditContext editor : context.ui.getTabManager().getRecordEditors()) {
			if (editor.hasUnsavedChanged()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getPassword() {
		return context.prefs.getPassword();
	}

	@Override
	public boolean isPasswordSaved() {
		return context.prefs.isPasswordSaved();
	}

	@Override
	public String getDataFilePath() {
		return context.prefs.getDataFilePath();
	}

	@Override
	public int getAesBits() {
		return context.prefs.getEncryptionStrengthBits();
	}

	@Override
	public JFrame getSuperFrame() {
		return context.ui.getMainFrame();
	}

	@Override
	public Action getCancelAction() {
		return new CancelPrefsAction();
	}

	@Override
	public boolean isDefaultPath(final String path) {
		// return true if the path did not change from what it was before
		String pathNoExt = BCUtil.getFileNameNoExt(path, true);
		String defaultNoExt = BCUtil.getFileNameNoExt(context.prefs.getDataFilePath(), true);
		return defaultNoExt.equals(pathNoExt);
	}

	@Override
	public boolean supportsChangeDataFileOptions() {
		return true;
	}

	@Override
	public String getPin() {
		return context.prefs.getPin();
	}

	@Override
	public boolean getUsePin() {
		return context.prefs.getUsePin();
	}

	@Override
	public int getPinTimeout() {
		return context.prefs.getPinTimeout();
	}

	@Override
	public int getPinMaxTry() {
		return context.prefs.getPinMaxTry();
	}

	class CancelPrefsAction extends AbstractAction {
		public CancelPrefsAction() {
			super("Cancel");
		}
		public void actionPerformed(ActionEvent e) {
			context.ui.getTabManager().removeOtherTab(context.ui.getPrefsComponent());
			context.ui.setPrefsComponent(null);
		}
	}

}
