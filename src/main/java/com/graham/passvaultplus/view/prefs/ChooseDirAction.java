/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.model.core.EncryptionHeader;
import com.graham.passvaultplus.model.core.PvpFileInterface;
import com.graham.passvaultplus.model.core.PvpFileReader;

public class ChooseDirAction extends AbstractAction {
	final private PreferencesContext context;
	final private JFrame parent;

	public ChooseDirAction(final PreferencesContext contextParam, final JFrame parentParam) {
		super("Choose File...");
		context = contextParam;
		parent = parentParam;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		final JFileChooser chooser = new JFileChooser();
		
		if (context.configAction == ConfigAction.Create || context.configAction == ConfigAction.Change) {
			final File f = context.getDataFile();
			if (f != null) {
				chooser.setSelectedFile(f);
			}
			final int returnVal = chooser.showSaveDialog(parent);
			if (returnVal == JFileChooser.APPROVE_OPTION) { 
				context.setDataFile(chooser.getSelectedFile(), 0);
			}
		} else if (context.configAction == ConfigAction.Open) {
			final int returnVal = chooser.showOpenDialog(parent);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				final File f = chooser.getSelectedFile();
				
				try {
					int encryptBits = 0;
					if (PvpFileInterface.isEncrypted(f.getName())) {
						final EncryptionHeader header = PvpFileReader.getEncryptHeader(f);
						encryptBits = header.aesStrengthBits;
					}
					context.setDataFile(f, encryptBits);
					context.setItemsDependentOnEncryptedEnabled();
					context.aesBits.setEnabled(false); // user cant change this when opening
				} catch (Exception e1) {
					context.conn.getPvpContext().notifyBadException(e1, true, false, PvpException.GeneralErrCode.CantReadEncryptionHeader);
				}
			}
		} else {
			throw new RuntimeException("unexpection action: " + context.configAction);
		}
	}
}
