/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;

public class EulaDialog {
	
	private JDialog d;
	private JCheckBox agreeCheckBox;
	
	public void showEula(final PvpContext context) {
		d = new JDialog(null, "Pass Vault Plus", Dialog.ModalityType.APPLICATION_MODAL);
		d.getContentPane().setLayout(new BorderLayout());
		
		final JLabel l1 = new JLabel("End User License Agrement");
		l1.setFont(l1.getFont().deriveFont(24.0f));
		l1.setBorder(new EmptyBorder(4,4,4,4));
		d.getContentPane().add(l1,  BorderLayout.NORTH);
		
		final JTextArea eulaText = new JTextArea();
		eulaText.setBorder(new EmptyBorder(4,4,4,4));
		eulaText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		eulaText.setEditable(false);
		eulaText.setText(context.getResourceText("eula-english"));
		eulaText.setLineWrap(true);
		eulaText.setWrapStyleWord(true);
		JScrollPane textScroll = new JScrollPane(eulaText);
		d.getContentPane().add(textScroll, BorderLayout.CENTER);
		
		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		agreeCheckBox = new JCheckBox("I Agree");
		buttonPanel.add(agreeCheckBox);
		buttonPanel.add(new JButton(new QuitAction()));
		buttonPanel.add(new JButton(new ContinueAction()));
		
		d.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		
		d.pack();
		
		d.setMinimumSize(new Dimension(340, 240));
		d.setPreferredSize(new Dimension(600, 400));
		d.setSize(new Dimension(600, 400));
		BCUtil.center(d);
		d.setVisible(true); // this is the line that causes the dialog to Block
	}
	
	static class QuitAction extends AbstractAction {
		QuitAction() {
			super("Quit");
		}
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	
	class ContinueAction extends AbstractAction {
		ContinueAction() {
			super("Continue");
		}
		public void actionPerformed(ActionEvent e) {
			if (agreeCheckBox.isSelected()) {
				d.setVisible(false);
			} else {
				JOptionPane.showMessageDialog(d, "You must select \"I Agree\" to continue");
			}
		}
	}

}
