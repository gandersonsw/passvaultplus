/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.graham.framework.BCUtil;
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
		final JPanel mainPanel = new JPanel(new BorderLayout());

		final JPanel p55 = new JPanel();
		p55.setLayout(new BoxLayout(p55, BoxLayout.PAGE_AXIS));
		final JLabel spacerLabel1 = new JLabel(" ");
		p55.add(spacerLabel1);

		{
			final JLabel l = new JLabel("Pass Vault Plus " + PvpContext.VERSION);
			l.setHorizontalAlignment(SwingConstants.CENTER);
			l.setFont(l.getFont().deriveFont(20.0f));
			final JPanel cp = new JPanel(new BorderLayout());
			cp.add(l, BorderLayout.CENTER);
			p55.add(cp);
			p55.add(new JLabel(" "));
		}

		{
			final JLabel l = new JLabel("Copyright © 2017 Graham Anderson.");
			l.setHorizontalAlignment(SwingConstants.CENTER);
			final JPanel cp = new JPanel(new BorderLayout());
			cp.add(l, BorderLayout.CENTER);
			p55.add(cp);
		}

		{
			final JLabel l = new JLabel("All rights reserved.");
			l.setHorizontalAlignment(SwingConstants.CENTER);
			final JPanel cp = new JPanel(new BorderLayout());
			cp.add(l, BorderLayout.CENTER);
			p55.add(cp);
			p55.add(new JLabel(" "));
		}

		{
			final JLabel l = new JLabel("Contact author at: " + EMAIL);
			l.setHorizontalAlignment(SwingConstants.CENTER);
			final JPanel cp = new JPanel(new BorderLayout());
			final JPanel emailPanel = new JPanel(new FlowLayout());
			emailPanel.add(l);
			final JButton ce = new JButton(new CopyEmail());
			BCUtil.makeButtonSmall(ce);
			emailPanel.add(ce);
			cp.add(emailPanel, BorderLayout.CENTER);
			p55.add(cp);
		}
		
		{
			final JTextArea helpLink = new JTextArea();
			final Font f1 = spacerLabel1.getFont().deriveFont(11.0f);
			helpLink.setBackground(spacerLabel1.getBackground());
			helpLink.setForeground(Color.BLUE);
			helpLink.setFont(f1);
			helpLink.setText("http://passvaultplus.com");
			helpLink.setEditable(false);
			helpLink.setBorder(new EmptyBorder(1, 24, 8, 8));
			final JPanel leftAlignPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
			leftAlignPanel2.add(helpLink);
			
			final JButton copyButton = new JButton(new CopyUrlAction(helpLink.getText()));
			BCUtil.makeButtonSmall(copyButton);
			copyButton.setFocusable(false);
			leftAlignPanel2.add(copyButton);
			if (Desktop.isDesktopSupported()) {
				final JButton gotoButton = new JButton(new GoToUrlAction(helpLink.getText()));
				BCUtil.makeButtonSmall(gotoButton);
				gotoButton.setFocusable(false);
				leftAlignPanel2.add(gotoButton);
			}
			
			p55.add(leftAlignPanel2);
		}
		
		{
			final JTextArea helpText = new JTextArea();
			helpText.setBackground(spacerLabel1.getBackground());
			helpText.setEditable(false);
			helpText.setText(getHelpText());
			helpText.setLineWrap(true);
			helpText.setWrapStyleWord(true);
			helpText.setPreferredSize(new Dimension(800, 2000));
			helpText.setMaximumSize(new Dimension(800, 2000));
			helpText.setMinimumSize(new Dimension(800, 2000));
			p55.add(helpText);
		}

		{
			final JPanel cp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			final JButton ch = new JButton(new CloseHelp());
			cp.add(ch);
			mainPanel.add(cp, BorderLayout.SOUTH);
		}

		final JScrollPane sp55 = new JScrollPane(p55);
		mainPanel.add(sp55, BorderLayout.CENTER);
		
		return mainPanel;
	}
	
	private String getHelpText() {
		InputStream sourceStream = null;
		InputStreamReader isr = null;
		BufferedReader bufR = null;
		try {
			if (PvpContext.JAR_BUILD) {
				// note path starts with "/" - that starts at the root of the jar,
				// instead of the location of the class.
				sourceStream = PvpContext.class.getResourceAsStream("/datafiles/help.txt");
				isr = new InputStreamReader(sourceStream);
			} else {
				File sourceFile = new File("datafiles/help.txt");
				isr = new FileReader(sourceFile);
			}
			
			bufR = new BufferedReader(isr);
			
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = bufR.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			return sb.toString();
		
		} catch (Exception e) {
			context.notifyWarning("WARN118 cant load help text", e);
			return "";
		} finally {
			if (bufR != null) {
				try { bufR.close(); } catch (Exception e) { }
			}
			if (isr != null) {
				try { isr.close(); } catch (Exception e) { }
			}
			if (sourceStream != null) {
				try { sourceStream.close(); } catch (Exception e) { }
			}
		}
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

	static class CopyUrlAction extends AbstractAction {
		final String url;
		public CopyUrlAction(final String urlParam) {
			super("Copy Link");
			url = urlParam;
		}
		public void actionPerformed(ActionEvent e) {
			final StringSelection ss = new StringSelection(url);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		}
	}
	
	static class GoToUrlAction extends AbstractAction {
		final String url;
		public GoToUrlAction(final String urlParam) {
			super("Go to Link");
			url = urlParam;
		}
		public void actionPerformed(ActionEvent evt) {
			if (Desktop.isDesktopSupported()) {
				URI uri;
				try {
					uri = new URI(url);
					Desktop.getDesktop().browse(uri);
				} catch (Exception ex) {
					// JOptionPane.showMessageDialog(eFrame, "There was an error opening link: " + ex.getMessage());
				}
			}
		}
	}
}
