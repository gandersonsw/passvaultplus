/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpException;

/**
 * When the app is first starting and the just the preferences are displayed.
 */
public class PreferencesConnectionStartup extends PreferencesConnection {
	
	final private JFrame startupOptionsFrame;
	final private String defaultDataFilePath;
	
	public PreferencesConnectionStartup(final PvpContext contextParam, final JFrame startupOptionsFrameParam) {
		super(contextParam);
		startupOptionsFrame = startupOptionsFrameParam;
		
		final String userHome = System.getProperty("user.home");
		final String fileSep = System.getProperty("file.separator");
		defaultDataFilePath = userHome + fileSep + "PassVaultPlusData" + fileSep + "pvp-data.xml";
	}

	@Override
	public Action getCancelAction() {
		return new QuitActionClass();
	}

	@Override
	public void doSave(final PrefsSettingsParam psp, final boolean wasChanges) {
		throw new RuntimeException("unsupported operation");
	}
	
	@Override
	public void doOpen(final PrefsSettingsParam psp) {
		setContextFromPsp(psp);
		startupOptionsFrame.setVisible(false);
		try {
			context.dataFileSelectedForStartup();
		} catch (Exception e1) {
			context.notifyBadException(e1, false, PvpException.GeneralErrCode.CantOpenMainWindow);
		}
	}

	@Override
	public JFrame getSuperFrame() {
		return startupOptionsFrame;
	}
	
	@Override
	public String getDataFilePath() {
		// when starting up, just use the default when asking, we don't want to save anything the user has entered that is not valid
		return defaultDataFilePath; 
	}
	
	@Override
	public String getPassword() {
		return "";
	}
	
	@Override
	public boolean isPasswordSaved() {
		return false;
	}
	@Override
	public int getAesBits() {
		return 128;
	}
	
	@Override
	public String getPin() {
		return "";
	}
	
	@Override
	public boolean getUsePin() {
		return false;
	}
	
	@Override
	public boolean isDefaultPath(final String path) {
		String pathNoExt = BCUtil.getFileNameNoExt(path, true);
		String defaultNoExt = BCUtil.getFileNameNoExt(defaultDataFilePath, true);
		return defaultNoExt.equals(pathNoExt);
	}
	
	class QuitActionClass extends AbstractAction {
		public QuitActionClass() {
			super("Quit");
		}
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}

}
