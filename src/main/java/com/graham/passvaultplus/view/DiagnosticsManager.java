/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.*;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.CommandExecuter;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.view.longtask.CancelableLongTask;
import com.graham.passvaultplus.view.longtask.LTCallback;
import com.graham.passvaultplus.view.longtask.LTRunnerAsync;
import com.graham.passvaultplus.view.longtask.LongTask;

public class DiagnosticsManager implements OtherTabBuilder {
	private static DiagnosticsManager activeManager = new DiagnosticsManager();

	private JTextArea ta;
	private StringBuilder log = new StringBuilder();
	private JTextField paramsTF;
	private JComboBox<String> commandCB;
	private JButton doIt;
	private CommandExecuter executer;

	public static DiagnosticsManager get() {
		return activeManager;
	}

	private DiagnosticsManager() {
	}

	public String getTitle() {
		return "Diagnostics";
	}

	public Component build(PvpContext context) {
		executer = new CommandExecuter(context, new CommandExeCallBack());
		JPanel mainP = new JPanel(new BorderLayout());
		ta = new JTextArea(log.toString());
		JScrollPane sp = new JScrollPane(ta);
		mainP.add(sp, BorderLayout.CENTER);

		JPanel commandP = new JPanel(new BorderLayout());
		commandCB = new JComboBox<>(executer.getCommands());
		commandP.add(commandCB, BorderLayout.WEST);
		paramsTF = new JTextField();
		commandP.add(paramsTF, BorderLayout.CENTER);
		doIt = new JButton(new DoCommandAction(executer));
		commandP.add(doIt, BorderLayout.EAST);
		mainP.add(commandP, BorderLayout.SOUTH);

		CommandSelectedAction csel = new CommandSelectedAction(executer);
		commandCB.addActionListener(csel);
		csel.actionPerformed(null);

		return mainP;
	}

	public void dispose() {
		ta = null;
		paramsTF = null;
		commandCB = null;
	}

	public void warning(final String s, final Exception e) {
		log.append(s);
		if (e != null) {
			log.append("::");
			log.append(e);
			log.append("::");
			log.append(e.getMessage());
			log.append("\n");
			log.append(BCUtil.getExceptionTrace(e));
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

	class DoCommandAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		final CommandExecuter executer;
		public DoCommandAction(final CommandExecuter e) {
			super("Execute");
			executer = e;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			executer.execute((String)commandCB.getSelectedItem(), paramsTF.getText());
		}
	}

	class CommandSelectedAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		final CommandExecuter executer;
		public CommandSelectedAction(final CommandExecuter e) {
			executer = e;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			paramsTF.setText(executer.getDefaultArguments((String)commandCB.getSelectedItem()));
		}
	}

		class CommandExeCallBack extends AbstractAction implements LTCallback {
			private LTRunnerAsync currentLt;
			public CommandExeCallBack() {
					super("Cancel");
			}
			Action oldAction;
				@Override
				public void taskStarting(LTRunnerAsync lt) {
						currentLt = lt;
						System.out.println("- - - CECB - - - taskStarting - - -");
						oldAction = doIt.getAction();
						doIt.setAction(this);
				}
				@Override
				public void taskComplete(LTRunnerAsync lt) {
						System.out.println("- - - CECB - - - taskComplete - - -");
						doIt.setAction(oldAction);
				}
				@Override
				public void handleException(LTRunnerAsync lt, Exception e) {
						System.out.println("- - - CECB - - - handleException - - -" + e.getMessage());
				}

				@Override
				public void actionPerformed(ActionEvent e) {
						currentLt.cancel();

				}
		}

}
