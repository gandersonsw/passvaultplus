/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

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
		// TODO clean this method up
		PvpFileReader fileReader = new PvpFileReader(context.getDataFile(), context);
		String rawXML = "";
		try {
			rawXML = BCUtil.dumpInputStreamToString(fileReader.getStream());
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			//context.notifyBadException("cannot load file", e, true);
			ex.printStackTrace();
		} finally {
			fileReader.close();
		}
		//} catch (UserAskToChangeFileException e2) {
		//	System.exit(0); // TODO test this
		//}

		final JFileChooser fc = new JFileChooser();
		int retVal = fc.showSaveDialog(null);
		if (retVal == JFileChooser.CANCEL_OPTION) {
			return;
		}
		final File f = fc.getSelectedFile();
		BCUtil.dumpStringToFile(rawXML, f);
	}
}
