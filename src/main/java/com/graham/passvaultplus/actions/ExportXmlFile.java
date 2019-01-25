/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpBackingStore;
import com.graham.passvaultplus.model.core.PvpInStreamer;

public class ExportXmlFile extends AbstractAction {
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
		final PvpInStreamer fileReader = new PvpInStreamer(backingStore, context);
		String rawXML = "";
		try {
			rawXML = BCUtil.dumpInputStreamToString(fileReader.getStream());
			System.out.println("xml size" + rawXML.length()); // don't use Diagnostics because this is error handle code
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Could not get XML: " + ex.getMessage());
			return;
		} finally {
			fileReader.close();
		}

		JTextArea te = new JTextArea();
		te.setText(rawXML);
		JScrollPane sp = new JScrollPane(te);
		JFrame f = new JFrame("XML");
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(sp, BorderLayout.CENTER);
		f.pack();
		BCUtil.setFrameSizeAndCenter(f, 700, 400);
		
		f.setVisible(true);

		/*
		 * DONT SAVE THE FILE - IT IS SECURE DATA !!!!!!
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
		*/
	}
}
