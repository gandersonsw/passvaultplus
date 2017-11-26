/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
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

/**
 * Password Dialog
 */
public class PwDialog {
	private JDialog d;
	private JTextField tf;
	private JPasswordField pf;
	private JCheckBox show;
	private PwAction actionHit;
	private boolean showConfigButton = true;

	public enum PwAction {
		Okay,
		Quit,
		Configure
	}
	
	public void setShowConfigButton(final boolean scb) {
		showConfigButton = scb;
	}
	
	public PwAction askForPw(final boolean passwordWasBad, final String resourseLocation) {
		d = new JDialog(null, "Pass Vault Plus", Dialog.ModalityType.APPLICATION_MODAL);
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
		
		if (passwordWasBad) {
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("That password is not correct. Please try again."));
			centerPanel.add(p);
		}
		
		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("Password:"));
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
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel file = new JLabel(resourseLocation);
			final Font fnt = file.getFont().deriveFont(file.getFont().getSize() - 1.0f);
			file.setFont(fnt);
			p.add(file);
			centerPanel.add(p);
		}
		
		{
			// add some spacers to make the button align to bottom and everythign else tot he top
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
			if (showConfigButton) {
				p.add(new JButton(new GoToSetupAction()));
			}
			final JButton okB = new JButton(new OkAction());
			p.add(okB);
			centerPanel.add(p);
			d.getRootPane().setDefaultButton(okB);
		}
		
		d.pack();
		BCUtil.center(d);
		d.setResizable(false);
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
