/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpFileReader;

public class ExportXmlFile extends AbstractAction {
	final private PvpContext context;

	public ExportXmlFile(PvpContext contextParam) {
		super("Save XML...");
		context = contextParam;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final PvpFileReader fileReader = new PvpFileReader(context.getDataFile(), context);
		String rawXML = "";
		try {
			rawXML = BCUtil.dumpInputStreamToString(fileReader.getStream());
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Could not get XML: " + ex.getMessage());
			return;
		} finally {
			fileReader.close();
		}

		final JFileChooser fc = new JFileChooser();
		int retVal = fc.showSaveDialog(null);
		if (retVal == JFileChooser.CANCEL_OPTION) {
			return;
		}
		final File f = fc.getSelectedFile();
		try {
			BCUtil.dumpStringToFile(rawXML, f);
		} catch (FileNotFoundException fnfe) {
			JOptionPane.showMessageDialog(null, "Could not save XML: " + fnfe.getMessage());
		}
	}
}
