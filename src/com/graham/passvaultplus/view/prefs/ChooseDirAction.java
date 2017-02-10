/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

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
		int returnVal;
		
		if (context.configAction == ConfigAction.Create || context.configAction == ConfigAction.Change) {
			final File f = context.getDataFile();
			if (f != null) {
				chooser.setSelectedFile(f);
			}
			//chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			returnVal = chooser.showSaveDialog(parent);
		} else if (context.configAction == ConfigAction.Open) {
			//chooser.setDialogType(JFileChooser.OPEN_DIALOG);
			//chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			returnVal = chooser.showOpenDialog(parent);
		} else {
			throw new RuntimeException("unexpection action: " + context.configAction);
		}
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			context.setDataFile(chooser.getSelectedFile());
		}
		
	}
}
