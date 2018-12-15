/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.FlowLayout;

import javax.swing.*;

import com.graham.passvaultplus.model.core.PvpBackingStoreGoogleDocs;

/**
 * Remote Backing Store Preference Handler
 * For now, the only one is Google. If more are added int he future, maybe need to add an enum.
 */
public class RemoteBSPrefHandler {
  final private PreferencesContext prefsContext;
  final boolean useGoogleDriveFlag; // this is not updated, original value only
  JCheckBox useGoogleDrive;

  public RemoteBSPrefHandler(PreferencesContext pcontext) {
    prefsContext = pcontext;
    useGoogleDriveFlag = prefsContext.conn.getContextPrefs().getUseGoogleDrive();
  }

  public JPanel buildPrefsUI() {
		final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		useGoogleDrive = new JCheckBox("Use Google™ Drive");
		useGoogleDrive.setSelected(useGoogleDriveFlag);
		p.add(useGoogleDrive);
		return p;
	}

  /** after prefs are changed and work is done, handle any cleanup */
  public void cleanup() {
    if (!useGoogleDrive.isSelected() && useGoogleDriveFlag) {
      prefsContext.conn.getPvpContextOriginal().ui.notifyInfo("deleteing Google Credientials if present");
      PvpBackingStoreGoogleDocs.deleteLocalCredentials();
    }
  }

  public boolean shouldSaveOnChange() {
    return useGoogleDrive.isSelected() && !useGoogleDriveFlag;
  }

  /** return true if continue */
  public boolean presave() {
    if (useGoogleDrive.isSelected() && !useGoogleDriveFlag) {
    //  PvpBackingStoreGoogleDocs bs = new PvpBackingStoreGoogleDocs();
      //bs.doChecksForNewFile();

    }
    return true;
  }


}
