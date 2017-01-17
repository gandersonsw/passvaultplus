/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.actions.*;

public class PreferencesBuilder {
	
	public static Component buildPrefs(final PvpContext contextParam) {
		return new PreferencesBuilder(contextParam).build();
	}
	
	final private PvpContext context;
	final private PreferencesContext prefsContext;
	
	private PreferencesBuilder(final PvpContext contextParam) {
		context = contextParam;
		prefsContext = new PreferencesContext(context);
	}
	
	private Component build() {
		JPanel p = new JPanel(new BorderLayout());
		p.add(buildTop(), BorderLayout.CENTER);
		p.add(buildBottom(p), BorderLayout.SOUTH);
		return p;
	}
	
	private Component buildBottom(final JPanel panelToBeReturned) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		p.add(prefsContext.errorMessage);
		p.add(new JButton(new CancelPrefsAction(context)));
		p.add(new JButton(new SavePrefsAction(context, prefsContext)));
		return p;
	}
	
	private Component buildTop() {
		EmptyBorder border1 = new EmptyBorder(0,40,0,0);
		
		JPanel p = new JPanel(new GridLayout(5,1));
		p.add(new JLabel("Save file options", JLabel.LEFT));
		
		JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p2.add(prefsContext.compressed);
		p2.add(prefsContext.encrypted);
		p2.add(prefsContext.savePassword);
		p2.setBorder(border1);
		p.add(p2);
		
		JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p3.add(new JLabel("Password:", JLabel.LEFT));
		p3.setBorder(border1);
		p3.add(prefsContext.password);
		p.add(p3);

		JPanel p4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p4.add(new JLabel("Data File:", JLabel.LEFT));
		prefsContext.dir = new JTextField(context.getDataFilePath());
		p4.add(prefsContext.dir);
		p.add(p4);

		JPanel p5 = new JPanel(new FlowLayout(FlowLayout.LEFT));

		AbstractAction defaultAction = new SetJTextFieldAction("Default", prefsContext.dir, context.getDataFilePath());
		JButton defBut = new JButton(defaultAction);
		p5.add(defBut);
		AbstractAction chooseDirAction = new ChooseDirAction(prefsContext.dir, context.getMainFrame());
		JButton chooseBut = new JButton(chooseDirAction);
		p5.add(chooseBut);

		p.add(p5);
		
		JPanel bp = new JPanel(new BorderLayout());
		bp.add(p, BorderLayout.NORTH);
		
		return bp;
	}
	
}
