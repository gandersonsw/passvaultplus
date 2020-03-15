/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import javax.swing.Action;
import javax.swing.JFrame;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpContextData;
import com.graham.passvaultplus.PvpContextPrefs;
import com.graham.passvaultplus.view.longtask.LTRunner;

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

	public abstract void doSave(LTRunner ltr, boolean wasChanges, PreferencesContext pc);

	public abstract void doOpen(LTRunner ltr, PreferencesContext pc);

	public abstract JFrame getSuperFrame();

	public abstract boolean isDefaultPath(String path);

	public boolean supportsChangeDataFileOptions() {
		return false;
	}

	protected void copyPrefsToReal() {
		PvpContextPrefs.copyPrefs(contextPrefsForSettingsUI, context.prefs, context);
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
