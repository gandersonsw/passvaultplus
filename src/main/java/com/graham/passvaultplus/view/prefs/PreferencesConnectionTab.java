/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpContextPrefsNoop;
import com.graham.passvaultplus.PvpContextPrefs;
import com.graham.passvaultplus.model.core.PvpPersistenceInterface;
import com.graham.passvaultplus.view.OtherTab;
import com.graham.passvaultplus.view.longtask.LTManager;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;

/**
 * When the preferences are displayed as a tab in the main panel.
 */
public class PreferencesConnectionTab extends PreferencesConnection {

	public PreferencesConnectionTab(final PvpContext contextParam) {
		super(contextParam, PvpContextPrefs.copyPrefs(contextParam.prefs, new PvpContextPrefsNoop(), null));
	}

	@Override
	public boolean doSave(final boolean wasChanges) {
		copyPrefsToReal();
		// if changes made don't require file rewrite, don't do it
		if (wasChanges) {
			//context.setDataFilePath(dataFile.getAbsolutePath());
			LTManager.runSync(context.data.getFileInterface().saveLT(context.data.getDataInterface(), PvpPersistenceInterface.SaveTrigger.major), "Saving...");
			//context.data.getFileInterface().save(context.data.getDataInterface(), PvpPersistenceInterface.SaveTrigger.major); // TODO if there is an exception here the changes from line 27 should not be applied
		}
		context.uiMain.hideTab(OtherTab.Prefs);
		return true;
	}

	@Override
	public boolean doOpen() {
		if (hasUnsavedChanges()) {
			boolean b = context.ui.showConfirmDialog("Unsaved changes", "There are some records that have been edited but not saved. Are you sure you want to discard them?");
			if (!b) {
				return false;
			}
		}

		// for now, dont support multiple databases open at once
		context.uiMain.getMainFrame().setVisible(false);
		copyPrefsToReal();

		PvpContext.startApp(false, contextPrefsForSettingsUI.getPassword()); // psp.pw TODO
		return true;
	}

	private boolean hasUnsavedChanges() {
		for (RecordEditContext editor : context.uiMain.getRecordEditors()) {
			if (editor.hasUnsavedChanged()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public JFrame getSuperFrame() {
		return context.uiMain.getMainFrame();
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

	class CancelPrefsAction extends AbstractAction {
		public CancelPrefsAction() {
			super("Cancel");
		}
		public void actionPerformed(ActionEvent e) {
			context.uiMain.hideTab(OtherTab.Prefs);
		}
	}

}
