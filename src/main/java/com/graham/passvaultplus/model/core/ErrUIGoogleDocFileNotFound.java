/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.view.longtask.LTManager;

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
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0009");
		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(buildCenter(), BorderLayout.CENTER);
		p1.add(buildBottom(), BorderLayout.SOUTH);
		p1.add(buildWest(), BorderLayout.WEST);

		d = new JDialog(context.uiMain.getMainFrame(), "File not found", true);
		d.setContentPane(p1);
		d.pack();
		d.setLocationRelativeTo(context.uiMain.getMainFrame());
		d.setVisible(true);
		return d;
	}

	private JPanel buildCenter() {
		final JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel a = new JLabel("Could not find file on Google Drive");
		a.setBorder(new EmptyBorder(10,1,4,12));
		p.add(a);
		centerPanel.add(p);

		JTextArea te = new JTextArea(
				"Expected to find file with ID:" + context.prefs.getGoogleDriveDocId() +
				"\nAlso looked for any files with the name: " + fileName +
				"\n\n When PassVaultPlus is exited, a new file will be created on Google Drive with all the data from the local file." +
				"\n\nOr if you wish, you can copy the file right now." +
				"\n\nIf you wish to not copy a file to Google Drive, uncheck \"Use Google Drive\" in the settings.");
		te.setEditable(false);
		final Font f1 = te.getFont().deriveFont(11.0f);
		te.setBorder(new EmptyBorder(4,4,4,14));
		te.setFont(f1);
		te.setBackground(centerPanel.getBackground());
		centerPanel.add(te);
		return centerPanel;
	}


	private JPanel buildBottom() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p.add(new JButton(new UploadNowAction()));
		p.add(new JButton(new DoneAction()));
		p.setBorder(new EmptyBorder(4,20,10,16));
		return p;
	}

	private JPanel buildWest() {
		ImageIcon icn = PvpContext.getIcon("option-pane-info", PvpContext.OPT_ICN_SCALE);
		JLabel icnLab = new JLabel(icn);
		icnLab.setBorder(new EmptyBorder(16, 25, 16, 24));
		JPanel p = new JPanel(new BorderLayout());
		p.add(icnLab, BorderLayout.NORTH);
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
			context.ui.enableQuitFromError(false);
			System.out.println("ErrUIGoogleDocFileNotFound.UploadNowAction.actionPerformed - getting ready to copy to google");
			// TODO test this
			// LTManager.runSync(context.data.getFileInterface().saveOneBackingStoreLT(context.data.getDataInterface(), backingStore), "Copy to Google Drive");
			//LTManager.runWithProgress(() -> context.data.getFileInterface().saveOneBackingStore(context.data.getDataInterface(), backingStore), "Uploading");
			LTManager.runWithProgress(context.data.getFileInterface().saveOneBackingStoreLT(context.data.getDataInterface(), backingStore), "Uploading");
			context.ui.enableQuitFromError(true);
		}
	}

}
