/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.jdom.JDOMException;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.UserAskToChangeFileException;
import com.graham.passvaultplus.model.core.PvpFileInterface;

public class ExportXmlFile extends AbstractAction {

	final private PvpFileInterface fi;

	public ExportXmlFile(PvpFileInterface paramfi) {
		super("Save XML...");
		fi = paramfi;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		final JFileChooser fc = new JFileChooser();
		int retVal = fc.showSaveDialog(null);
		if (retVal == JFileChooser.CANCEL_OPTION) {
			return;
		}
		File f = fc.getSelectedFile();

		try {
			String xml = fi.getJdomDoc(true).getRawXml();
			BCUtil.dumpStringToFile(xml, f);
		} catch (JDOMException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (UserAskToChangeFileException e2) {
			System.exit(0); // TODO test this
		}
	}
}
