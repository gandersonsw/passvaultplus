/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.*;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpBackingStore;
import com.graham.passvaultplus.model.core.PvpInStreamer;
import com.graham.passvaultplus.view.longtask.LTManager;
import com.graham.passvaultplus.view.longtask.LTRunner;
import com.graham.passvaultplus.view.longtask.LongTask;
import com.graham.util.GenUtil;
import com.graham.util.SwingUtil;

public class ExportXmlFile extends AbstractAction implements LongTask {
	private static final long serialVersionUID = 1L;
	final private PvpContext context;
	final private PvpBackingStore backingStore;

	public ExportXmlFile(PvpContext contextParam, PvpBackingStore bs) {
		super("View XML...");
		context = contextParam;
		backingStore = bs;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		LTManager.runWithProgress(this, "Export Xml");
	}

	@Override
	public void runLongTask(LTRunner ltr) {
		final PvpInStreamer fileReader = new PvpInStreamer(backingStore, context);
		try {
			String rawXML = GenUtil.dumpInputStreamToString(fileReader.getStream(ltr));
			System.out.println("ExportXmlFile.actionPerformed - xml size:" + rawXML.length()); // don't use Diagnostics because this is error handle code

			SwingUtilities.invokeAndWait(() -> {
				JTextArea te = new JTextArea();
				te.setText(rawXML);
				JScrollPane sp = new JScrollPane(te);
				JFrame f = new JFrame("XML");
				f.getContentPane().setLayout(new BorderLayout());
				f.getContentPane().add(sp, BorderLayout.CENTER);
				f.pack();
				SwingUtil.setFrameSizeAndCenter(f, 700, 400);

				f.setVisible(true);
			});
		} catch (Exception ex) {
			context.ui.showErrorDialog("Error", "Could not get XML: " + ex.getMessage());
			return;
		} finally {
			fileReader.close();
		}

	}
}
