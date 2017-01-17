/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.graham.passvaultplus.PvpContext;

public class HelpAction extends AbstractAction {
	
	final public static String EMAIL = "gandersonsw@gmail.com";
	
	private final PvpContext context;
	private JPanel helpPanel;

	public HelpAction(PvpContext contextParam) {
		super(null, PvpContext.getIcon("help"));
		context = contextParam;
	}

	public void actionPerformed(ActionEvent e) {
		if (helpPanel == null) {
			helpPanel = buildHelp();
			context.getTabManager().addOtherTab("Help", helpPanel);
		}
		context.getTabManager().setSelectedComponent(helpPanel);
	}
	
	private JPanel buildHelp() {
		JPanel mainPanel = new JPanel(new BorderLayout());

		JPanel p55 = new JPanel();
		p55.setLayout(new BoxLayout(p55, BoxLayout.PAGE_AXIS));
		p55.add(new JLabel(" "));

		JLabel l = new JLabel("Pass Vault Plus 1.0 Beta");
		l.setHorizontalAlignment(SwingConstants.CENTER);
		l.setFont(l.getFont().deriveFont(20.0f));
		JPanel cp = new JPanel(new BorderLayout());
		cp.add(l, BorderLayout.CENTER);
		p55.add(cp);
		p55.add(new JLabel(" "));

		l = new JLabel("Copyright © 2017 Graham Anderson.");
		l.setHorizontalAlignment(SwingConstants.CENTER);
		cp = new JPanel(new BorderLayout());
		cp.add(l, BorderLayout.CENTER);
		p55.add(cp);

		l = new JLabel("All rights reserved.");
		l.setHorizontalAlignment(SwingConstants.CENTER);
		cp = new JPanel(new BorderLayout());
		cp.add(l, BorderLayout.CENTER);
		p55.add(cp);
		p55.add(new JLabel(" "));

		l = new JLabel("Contact author at: " + EMAIL);
		l.setHorizontalAlignment(SwingConstants.CENTER);
		cp = new JPanel(new BorderLayout());
		cp.add(l, BorderLayout.CENTER);
		p55.add(cp);
		p55.add(new JLabel(" "));

		JButton ce = new JButton(new CopyEmail());
		cp = new JPanel();
		cp.add(ce);
		JButton ch = new JButton(new CloseHelp());
		//cp = new JPanel();
		cp.add(ch);
		p55.add(cp);

		mainPanel.add(p55, BorderLayout.NORTH);
		
		return mainPanel;
	}

	static class CopyEmail extends AbstractAction {
		public CopyEmail() {
			super("Copy Email");
		}
		public void actionPerformed(ActionEvent e) {
			StringSelection ss = new StringSelection(EMAIL);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		}
	}
	
	class CloseHelp extends AbstractAction {
		public CloseHelp() {
			super("Close");
		}
		public void actionPerformed(ActionEvent e) {
			context.getTabManager().removeOtherTab(helpPanel);
			helpPanel = null;
		}
	}
}
