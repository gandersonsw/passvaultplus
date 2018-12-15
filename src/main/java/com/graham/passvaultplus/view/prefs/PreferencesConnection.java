/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import javax.swing.Action;
import javax.swing.JFrame;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpContextPrefs;

/**
 * Way to give the preferences stuff a connection to the outside world.
 */
public abstract class PreferencesConnection {

	protected final PvpContext context; // The real context
	protected final PvpContextPrefs contextPrefsForSettingsUI; // a temporary context prefs that has been edited

	public PreferencesConnection(final PvpContext contextParam, final PvpContextPrefs cp) {
		context = contextParam;
		contextPrefsForSettingsUI = cp;
	}

	public abstract Action getCancelAction();

	public abstract boolean doSave(final boolean wasChanges);

	public abstract boolean doOpen();

	public abstract JFrame getSuperFrame();

	public abstract boolean isDefaultPath(final String path);

	public boolean supportsChangeDataFileOptions() {
		return false;
	}

	protected void copyPrefsToReal() {
		PvpContextPrefs.copyPrefs(contextPrefsForSettingsUI, context.prefs);
	}

	public PvpContextPrefs getContextPrefs() {
		return contextPrefsForSettingsUI;
	}

	public PvpContext getPvpContextOriginal() {
		return context;
	}

	public PvpContextPrefs getContextPrefsOriginal() {
		return context.prefs;
	}

}
