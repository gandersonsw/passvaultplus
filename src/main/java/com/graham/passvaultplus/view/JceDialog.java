/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.actions.GoToUrlAction;
import com.graham.passvaultplus.PvpException;

public class JceDialog {
	private JDialog d;
	
	public static String generateErrorDescription() {
		final String javaHome = System.getProperty("java.home");
		final String fs = System.getProperty("file.separator");
		return "Strong encryption is not enabled. \n" + 
				"To enable it search for \"JCE\" (Java Cryptography Extension) or go to:\n" +
				"http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html\n" +
				"After you have downloaded \"jce_policy-8.zip\", expand it and copy \"local_policy.jar\" and \"US_export_policy.jar\" \nto " + javaHome  + fs + "lib" + fs + "security";
	}
	
	public void showDialog(final Window parent, int maxKeySize) {
		d = new JDialog(parent, "Pass Vault Plus : JCE", Dialog.ModalityType.APPLICATION_MODAL);
		
		d.getContentPane().setLayout(new BoxLayout(d.getContentPane(), BoxLayout.Y_AXIS));
		
		final String mksd = maxKeySize > 0 ? "Current maximum key size:" + maxKeySize + " bits" : "";
		final JLabel errorMsg = new JLabel("Strong encryption is not enabled. " + mksd);
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(errorMsg);
			d.getContentPane().add(p);
		}
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("To enable it search for \"JCE\" or go to:"));
			d.getContentPane().add(p);
		}
		
		{
			JPanel p = createLink("http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html", errorMsg.getBackground());
			d.getContentPane().add(p);
		}
		
		{
			final String javaHome = System.getProperty("java.home");
			final String fs = System.getProperty("file.separator");
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			final JTextArea linkTE = new JTextArea();
			final Font f1 = linkTE.getFont().deriveFont(11.0f);
			linkTE.setBackground(errorMsg.getBackground());
			linkTE.setFont(f1);
			linkTE.setText("After you have downloaded \"jce_policy-8.zip\", expand it and copy \"local_policy.jar\" and \"US_export_policy.jar\" \nto " + javaHome  + fs + "lib" + fs + "security");
			linkTE.setEditable(false);
			linkTE.setBorder(new EmptyBorder(1, 24, 8, 8));
			p.add(linkTE);
			d.getContentPane().add(p);
		}
		
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("Pass Vault Plus Help page on this:"));
			d.getContentPane().add(p);
		}
		
		{
			JPanel p = createLink(ErrorFrame.PVP_HELP_URL_ERR_BASE + PvpException.GeneralErrCode.InvalidKey.getHelpId(), errorMsg.getBackground());
			d.getContentPane().add(p);
		}
		
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			final JButton okB = new JButton(new OkAction());
			p.add(okB);
			d.getContentPane().add(p);
			d.getRootPane().setDefaultButton(okB);
		}
		
		d.pack();
		//BCUtil.center(d);
		d.setLocationRelativeTo(parent);
		d.setVisible(true); // this is the line that causes the dialog to Block
	}
	
	private JPanel createLink(final String linkStr, Color bgColor) {
		final JTextArea linkTE = new JTextArea();
		final Font f1 = linkTE.getFont().deriveFont(11.0f);
		linkTE.setBackground(bgColor);
		linkTE.setForeground(Color.BLUE);
		linkTE.setFont(f1);
		linkTE.setText(linkStr);
		linkTE.setEditable(false);
		linkTE.setBorder(new EmptyBorder(1, 24, 8, 8));
		final JPanel leftAlignPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		leftAlignPanel2.add(linkTE);
		
		final JButton copyButton = new JButton(new CopyUrlAction(linkTE));
		BCUtil.makeButtonSmall(copyButton);
		copyButton.setFocusable(false);
		leftAlignPanel2.add(copyButton);
		GoToUrlAction.checkAndAdd(leftAlignPanel2, linkStr);
		
		return leftAlignPanel2;
	}
	
	class OkAction extends AbstractAction {
		OkAction() {
			super("Continue");
		}
		public void actionPerformed(ActionEvent e) {
			d.setVisible(false);
		}
	}
	
	class CopyUrlAction extends AbstractAction {
		final private JTextArea link;
		public CopyUrlAction(final JTextArea linkParam) {
			super("Copy Link");
			link = linkParam;
		}
		public void actionPerformed(ActionEvent e) {
			final StringSelection ss = new StringSelection(link.getText());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		}
	}

}
