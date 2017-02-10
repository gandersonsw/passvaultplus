/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;

/**
 * When the preferences are displayed as a tab in the main panel.
 */
public class PreferencesConnectionTab extends PreferencesConnection {

	public PreferencesConnectionTab(final PvpContext contextParam) {
		super(contextParam);
	}
	
	@Override
	public void doSave(final File dataFile, final boolean wasChanges) {
		// if changes made don't require file rewrite, don't do it
		if (wasChanges) {
			context.setDataFilePath(dataFile.getAbsolutePath());
			context.getFileInterface().save(context.getDataInterface());
		}

		context.getTabManager().removeOtherTab(context.getPrefsComponent());
		context.setPrefsComponent(null);
	}
	
	@Override
	public void doOpen(final File dataFile) {
		// TODO
		System.out.println("open new database TODO");
	}
	
	@Override
	public String getDataFilePath() {
		return context.getDataFilePath();
	}
	
	@Override
	public JFrame getSuperFrame() {
		return context.getMainFrame();
	}
	
	@Override
	public Action getCancelAction() {
		return new CancelPrefsAction();
	}
	
	@Override
	public boolean isDefaultPath(final String path) {
		// return true if the path did not change from what it was before 
		String pathNoExt = BCUtil.getFileNameNoExt(path, true);
		String defaultNoExt = BCUtil.getFileNameNoExt(context.getDataFilePath(), true);
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
			context.getTabManager().removeOtherTab(context.getPrefsComponent());
			context.setPrefsComponent(null);
		}
	}

}
