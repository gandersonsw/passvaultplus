/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpContextUI;
import com.graham.passvaultplus.view.prefs.PreferencesBuilder;
import com.graham.passvaultplus.view.prefs.PreferencesConnectionStartup;

public class StartupOptionsFrame extends JFrame {

	public static void showAndContinue(final PvpContext context) {
		javax.swing.SwingUtilities.invokeLater(() -> new StartupOptionsFrame(context));
	}
	
	public StartupOptionsFrame(final PvpContext context) {
		super("Pass Vault Plus: Configuration");
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0191");
		Component c = PreferencesBuilder.buildPrefs(new PreferencesConnectionStartup(context, this));
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(c, BorderLayout.CENTER);
		
		final JLabel logo = new JLabel(PvpContext.getIcon("pvplogo24pt90deg"));
		logo.setBorder(new EmptyBorder(5,3,0,1));
		final JPanel westLayout = new JPanel(new BorderLayout());
		westLayout.add(logo, BorderLayout.NORTH);
		this.getContentPane().add(westLayout, BorderLayout.WEST);
		
		addWindowListener(new ExitOnCloseAdapter());
		pack();
		BCUtil.center(this);
		setResizable(false);
		PvpContextUI.getActiveUI().setFrame(this);
		setVisible(true);
	}
	
	class ExitOnCloseAdapter extends java.awt.event.WindowAdapter {
		public void windowClosing(java.awt.event.WindowEvent event) {
			System.exit(0);
		}
	}

}
