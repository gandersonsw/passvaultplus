/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class ChooseDirAction extends AbstractAction {
	final private JTextField dir;
	final private JFrame parent;

	public ChooseDirAction(final JTextField dirParam, final JFrame parentParam) {
		super("Choose File...");
		dir = dirParam;
		parent = parentParam;
	}
	public void actionPerformed(ActionEvent e) {
		// TODO probably allow choose of file or directory
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int returnVal = chooser.showOpenDialog(parent);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			dir.setText(chooser.getSelectedFile().getPath());
		}
	}
}
