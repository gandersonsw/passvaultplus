/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.graham.framework.BCUtil;

/**
 * Password Dialog
 */
public class PwDialog {
	private JDialog d;
	private JTextField tf;
	private JPasswordField pf;
	private JCheckBox show;
	private PwAction actionHit;

	public enum PwAction {
		Okay,
		Quit,
		Configure
	}
	
	public PwDialog() {
	}
	
	public PwAction askForPw(final boolean passwordWasBad, final String path) {
		d = new JDialog(null, "Pass Vault Plus", Dialog.ModalityType.APPLICATION_MODAL);
		
		d.getContentPane().setLayout(new BoxLayout(d.getContentPane(), BoxLayout.Y_AXIS));
		
		if (passwordWasBad) {
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("That password is not correct. Please try again."));
			d.getContentPane().add(p);
		}
		
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("Password:"));
			d.getContentPane().add(p);
		}
	
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			//Box.createRigidArea(indentDim)
			tf = new JTextField(27);
			tf.setVisible(false);
			pf = new JPasswordField(27);
			p.add(tf);
			p.add(pf);
			d.getContentPane().add(p);
		}
		
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			show = new JCheckBox("Show");
			p.add(show);
			d.getContentPane().add(p);
			show.addActionListener(new ShowPwAction());
		}
	
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("File: " + path));
			d.getContentPane().add(p);
		}
	
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			p.add(new JButton(new QuitAction()));
			p.add(new JButton(new GoToSetupAction()));
			final JButton okB = new JButton(new OkAction());
			p.add(okB);
			d.getContentPane().add(p);
			d.getRootPane().setDefaultButton(okB);
		}
		
		d.pack();
		BCUtil.center(d);
		d.setVisible(true); // this is the line that causes the dialog to Block
		return actionHit;
	}
	
	public String getPw() {
		if (show.isSelected()) {
			return tf.getText();
		} else {
			return new String(pf.getPassword());
		}
	}
	
	static class QuitAction extends AbstractAction {
		QuitAction() {
			super("Quit");
		}
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	
	class OkAction extends AbstractAction {
		OkAction() {
			super("Continue");
		}
		public void actionPerformed(ActionEvent e) {
			actionHit = PwAction.Okay;
			d.setVisible(false);
		}
	}
	
	class GoToSetupAction extends AbstractAction {
		GoToSetupAction() {
			super("Go to Setup");
		}
		public void actionPerformed(ActionEvent e) {
			actionHit = PwAction.Configure;
			d.setVisible(false);
		}
	}
	
	class ShowPwAction extends AbstractAction {
		public ShowPwAction() {
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (show.isSelected()) {
				String pw = new String(pf.getPassword());
				tf.setText(pw);
				pf.setVisible(false);
				tf.setVisible(true);
				tf.getParent().revalidate();
			} else {
				String pw = tf.getText();
				pf.setText(pw);
				pf.setVisible(true);
				tf.setVisible(false);
				pf.revalidate();
			}
		}
	}

}
