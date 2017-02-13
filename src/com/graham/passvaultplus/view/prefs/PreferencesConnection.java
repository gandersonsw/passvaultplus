/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import javax.swing.Action;
import javax.swing.JFrame;

import com.graham.passvaultplus.PvpContext;

/**
 * Way to give the preferences stuff a connection to the outside world.
 */
public abstract class PreferencesConnection {

	protected final PvpContext context;
	
	public PreferencesConnection(final PvpContext contextParam) {
		context = contextParam;
	}
	
	public abstract Action getCancelAction();
	
	public String getPassword() {
		return context.getPassword(); // TODO
	}
	
	public boolean isPasswordSaved() {
		return context.isPasswordSaved();
	}
	
	public void setPassword(final String passwordParam, final boolean makePersistant) {
		context.setPassword(passwordParam, makePersistant);
	}
	
	public abstract String getDataFilePath();
	
	public abstract int getAesBits();
	
	public abstract void doSave(final PrefsSettingsParam psp, final boolean wasChanges);
	
	public abstract void doOpen(final PrefsSettingsParam psp);
	
	public abstract JFrame getSuperFrame();
	
	public abstract boolean isDefaultPath(final String path);
	
	public boolean supportsChangeDataFileOptions() {
		return false;
	}
	
	protected void setContextFromPsp(final PrefsSettingsParam psp) {
		context.setDataFilePath(psp.f.getAbsolutePath(), psp.aesBits); // TODO check that the aesBits works here
		if (psp.spw) { // persist password
			context.setPassword(psp.pw, true);
		} else {
			context.setPassword("", true); // clear persisted value
			context.setPassword(psp.pw, false);
		}
	}
}
