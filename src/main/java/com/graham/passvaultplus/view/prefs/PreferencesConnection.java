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
	
	public abstract String getPassword();
	
	public abstract boolean isPasswordSaved();
	
	public abstract String getPin();
	
	public abstract boolean getUsePin();
	
	public int getPinTimeout() {
		return 30; // 30 minutes is the default
	}
	
	public int getPinMaxTry() {
		return 5; // 5 trys is the default
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
	
	public PvpContext getPvpContext() {
		return context;
	}
	
	protected void setContextFromPsp(final PrefsSettingsParam psp) {
		context.setDataFilePath(psp.f.getAbsolutePath(), psp.aesBits);
		context.setPasswordAndPin(psp.pw, psp.spw, psp.pin, psp.usePin);
		context.setPinTimeout(psp.pinTimeout);
		context.setPinMaxTry(psp.pinMaxTry);
		context.setShowDashboard(psp.showDashBoard);
		context.setUseGoogleDrive(psp.useGoogleDrive);
		context.setShowDiagnostics(psp.showDiagnostics);
	}
	
	public boolean getShowDashboard() {
		return context.getShowDashboard();
	}
	
	public boolean getUseGoogleDrive() {
		return context.getUseGoogleDrive();
	}
	
	public boolean getShowDiagnostics() {
		return context.getShowDiagnostics();
	}
}
