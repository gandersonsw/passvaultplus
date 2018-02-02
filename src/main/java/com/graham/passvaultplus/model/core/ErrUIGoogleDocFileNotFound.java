/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.graham.passvaultplus.PvpContext;

public class ErrUIGoogleDocFileNotFound {
	
	final private PvpContext context;
	final private String fileName;
	final private PvpBackingStore backingStore;
	private JDialog d;
	
	public ErrUIGoogleDocFileNotFound(final PvpContext contextParam, final String fileNameParam, final PvpBackingStore backingStoreParam) {
		context = contextParam;
		fileName = fileNameParam;
		backingStore = backingStoreParam;
	}
	
	public JDialog buildDialog() {
		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(buildTop(), BorderLayout.NORTH);
		p1.add(buildCenter(), BorderLayout.CENTER);
		p1.add(buildBottom(), BorderLayout.SOUTH);
		
		d = new JDialog(context.getMainFrame(), "File not found", true);
		d.setContentPane(p1);
		d.pack();
		d.setLocationRelativeTo(context.getMainFrame());
		d.setVisible(true);
		return d;
	}
	
	public JPanel buildTop() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel a = new JLabel("Could not find file on Google Drive");
		p.add(a);
		return p;
	}
	
	public JPanel buildCenter() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JTextArea te = new JTextArea(
				"Expected to find file with ID:" + context.getGoogleDriveDocId() + 
				"\nAlso looked for any files with the name: " + fileName +
				"\n\n When PassVaultPlus is exited, a new file will be created on Google Drive with all the data from the local file." +
				"\n\nOr if you wish, you can copy the file right now." +
				"\n\nIf you wish to not copy a file to Google Drive, uncheck \"Use Google Drive\" in the settings.");
		te.setEditable(false);
		final Font f1 = te.getFont().deriveFont(11.0f);
		te.setFont(f1);
		p.add(te);
		return p;
	}
	
	
	public JPanel buildBottom() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p.add(new JButton(new UploadNowAction()));
		p.add(new JButton(new DoneAction()));
		return p;
	}
	
	public class DoneAction extends AbstractAction {
		public DoneAction() {
			super("Done");
		}
		public void actionPerformed(ActionEvent e) {
			d.setVisible(false);
		}
	}
	
	public class UploadNowAction extends AbstractAction {
		public UploadNowAction() {
			super("Copy to Google Drive Now");
		}
		public void actionPerformed(ActionEvent e) {
			this.setEnabled(false);
			d.setVisible(false);
			context.enableQuitFromError(false);
			System.out.println("getting ready to copy to gggole");
			context.getFileInterface().saveOneBackingStore(context.getDataInterface(), backingStore);
			context.enableQuitFromError(true);
			
			//context.getTabManager().removeOtherTab(panelInTabPane);
		}
	}

}
