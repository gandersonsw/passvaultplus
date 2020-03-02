/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;

import javax.swing.*;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpContextPrefsNoop;
import com.graham.passvaultplus.PvpContextUI;
import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.view.longtask.LTManager;

/**
 * When the app is first starting and the just the preferences are displayed.
 */
public class PreferencesConnectionStartup extends PreferencesConnection {

	final private JFrame startupOptionsFrame;
	final private String defaultDataFilePath;

	public PreferencesConnectionStartup(final PvpContext contextParam, final JFrame startupOptionsFrameParam) {
		super(contextParam, new PvpContextPrefsNoop());
		startupOptionsFrame = startupOptionsFrameParam;

		final String userHome = System.getProperty("user.home");
		final String fileSep = System.getProperty("file.separator");
		defaultDataFilePath = userHome + fileSep + "PassVaultPlusData" + fileSep + "pvp-data.xml";
		contextPrefsForSettingsUI.setDataFilePath(defaultDataFilePath, contextPrefsForSettingsUI.getEncryptionStrengthBits());
	}

	@Override
	public Action getCancelAction() {
		return new QuitActionClass();
	}

	@Override
	public void doSave(final boolean wasChanges, PreferencesContext pc) {
		throw new RuntimeException("unsupported operation");
	}

	@Override
	public void doOpen(PreferencesContext pc) {

		//	com.graham.passvaultplus.PvpContextUI.checkEvtThread("3524");
			copyPrefsToReal();
			if (SwingUtilities.isEventDispatchThread()) {
					startupOptionsFrame.setVisible(false);
			} else {
					try { SwingUtilities.invokeAndWait(() -> startupOptionsFrame.setVisible(false)); } catch (Exception e) { PvpContextUI.getActiveUI().notifyWarning("PreferencesConnectionStartup.doOpen.A"); }
			}
			//startupOptionsFrame.setVisible(false);
			try {
				context.dataFileSelectedForStartup();
				pc.cleanup();
				//return true;
			} catch (Exception e1) {
				context.ui.notifyBadException(e1, false, PvpException.GeneralErrCode.CantOpenMainWindow);
				//return false;
			}

	}

	@Override
	public JFrame getSuperFrame() {
		return startupOptionsFrame;
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
