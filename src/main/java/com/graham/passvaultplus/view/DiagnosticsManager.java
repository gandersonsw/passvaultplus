/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.graham.passvaultplus.PvpContext;

public class DiagnosticsManager {
	
	private Component diagnosticsComponent;
	private JTextArea ta;
	private StringBuilder log = new StringBuilder();
	
	public void checkDiagnostics() {
		PvpContext context = PvpContext.getActiveContext();
		if (context.getShowDiagnostics() && diagnosticsComponent == null) {
			try {
				diagnosticsComponent = buildDiagnosticsTab();
				context.getTabManager().addOtherTab("Diagnostics", diagnosticsComponent);
			} catch (Exception e) {
				// if the Diagnostics fails to load, dont crash the app
				e.printStackTrace();
			}
		} else if (!context.getShowDashboard() && diagnosticsComponent != null) {
			context.getTabManager().removeOtherTab(diagnosticsComponent);
			diagnosticsComponent = null;
			ta = null;
		}
	}
	
	private Component buildDiagnosticsTab() {
		ta = new JTextArea(log.toString());
		JScrollPane sp = new JScrollPane(ta);
		return sp;
	}
	
	public void warning(final String s, final Exception e) {
		log.append(s);
		if (e != null) {
			log.append("::");
			log.append(e.getMessage());
		}
		log.append("\n");
		logUpdated();
	}
	
	public void info(String s) {
		log.append(s);
		log.append("\n");
		logUpdated();
	}
	
	private void logUpdated() {
		if (log.length() > 100000) {
			log = new StringBuilder("<<< log trimmed >>>" + log.substring(50000));
		}
		if (ta != null) {
			ta.setText(log.toString());
		}
	}

}
