/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.Component;

import javax.swing.JFrame;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.view.prefs.PreferencesBuilder;
import com.graham.passvaultplus.view.prefs.PreferencesConnectionStartup;

public class StartupOptionsFrame extends JFrame {
	
	public StartupOptionsFrame(final PvpContext context) {
		super("Pass Vault Plus: Configuration");
		Component c = PreferencesBuilder.buildPrefs(new PreferencesConnectionStartup(context, this));
		this.getContentPane().add(c);
		addWindowListener(new ExitOnCloseAdapter());
		pack();
		BCUtil.center(this);
		setResizable(false);
		setVisible(true);
	}
	
	class ExitOnCloseAdapter extends java.awt.event.WindowAdapter {
		public void windowClosing(java.awt.event.WindowEvent event) {
			System.exit(0);
		}
	}

}
