/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.BorderLayout;
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
import javax.swing.border.EmptyBorder;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpContextUI;

public class PinDialog {
	private JDialog d;
	private JTextField tf;
	private JPasswordField pf;
	private JCheckBox show;
	private PinAction actionHit;
	private boolean showConfigButton = true;

	public enum PinAction {
		Okay,
	//	Quit, there is a quit button, but System.exit is called in that case
		Configure,
		UsePassword
	}
	
	public void setShowConfigButton(final boolean scb) {
		showConfigButton = scb;
	}
	
	public PinAction askForPin(final int pinWasBadCount) {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0157");
		d = PvpContextUI.createDialog("Pass Vault Plus");
		//d = new JDialog(null, "Pass Vault Plus", Dialog.ModalityType.APPLICATION_MODAL);
		d.getContentPane().setLayout(new BorderLayout());
		
		final JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		d.getContentPane().add(centerPanel, BorderLayout.CENTER);
		
		{
			final JLabel logo = new JLabel(PvpContext.getIcon("pvplogo24pt90deg"));
			logo.setBorder(new EmptyBorder(5,3,0,6));
			final JPanel westLayout = new JPanel(new BorderLayout());
			westLayout.add(logo, BorderLayout.NORTH);
			d.getContentPane().add(westLayout, BorderLayout.WEST);
		}
		
		if (pinWasBadCount > 0) {
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("That PIN is not correct. Please try again."));
			centerPanel.add(p);
		}
		
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("PIN:"));
			centerPanel.add(p);
		}
	
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			//Box.createRigidArea(indentDim)
			tf = new JTextField(27);
			tf.setVisible(false);
			pf = new JPasswordField(27);
			p.add(tf);
			p.add(pf);
			centerPanel.add(p);
		}
		
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			show = new JCheckBox("Show");
			p.add(show);
			centerPanel.add(p);
			show.addActionListener(new ShowPwAction());
		}
		
		{
			// add some spacers to make the button align to bottom and everything else to the top
			final JPanel sp1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			sp1.add(new JLabel(" "));
			centerPanel.add(sp1);
			final JPanel sp2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			sp2.add(new JLabel(" "));
			centerPanel.add(sp2);
		}
	
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			p.add(new JButton(new QuitAction()));
			p.add(new JButton(new GoToPasswordAction()));
			if (showConfigButton) {
				p.add(new JButton(new GoToSetupAction()));
			}
			final JButton okB = new JButton(new OkAction());
			p.add(okB);
			centerPanel.add(p);
			d.getRootPane().setDefaultButton(okB);
		}

		d.setResizable(false);
		PvpContextUI.showDialog(d); // this is the line that causes the dialog to Block
		return actionHit;
	}
	
	public String getPin() {
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
			actionHit = PinAction.Okay;
			//d.setVisible(false);
			PvpContextUI.hideDialog(d);
		}
	}
	
	class GoToSetupAction extends AbstractAction {
		GoToSetupAction() {
			super("Go to Setup");
		}
		public void actionPerformed(ActionEvent e) {
			actionHit = PinAction.Configure;
			//d.setVisible(false);
			PvpContextUI.hideDialog(d);
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
	
	class GoToPasswordAction extends AbstractAction {
		GoToPasswordAction() {
			super("Use Password");
		}
		public void actionPerformed(ActionEvent e) {
			actionHit = PinAction.UsePassword;
			//d.setVisible(false);
			PvpContextUI.hideDialog(d);
		}
	}
}
