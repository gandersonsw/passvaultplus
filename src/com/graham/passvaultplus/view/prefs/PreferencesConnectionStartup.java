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
	public void doSave(final File dataFile, final boolean wasChanges) {
		throw new RuntimeException("unsupported operation");
	}
	
	@Override
	public void doOpen(final File dataFile) {
		context.setDataFilePath(dataFile.getAbsolutePath());
		startupOptionsFrame.setVisible(false);
		
		try {
			context.dataFileSelectedForStartup();
		} catch (Exception e1) {
			System.out.println("- - - - - DataFileDir OK - - - - - - -");
			// TODO Auto-generated catch block
			e1.printStackTrace();
			startupOptionsFrame.setVisible(true);
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
