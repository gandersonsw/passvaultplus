/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.*;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.AppUtil;
import com.graham.passvaultplus.CommandExecuter;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.view.longtask.LTCallback;
import com.graham.passvaultplus.view.longtask.LTRunner;
import com.graham.passvaultplus.view.longtask.LTRunnerAsync;

public class DiagnosticsManager implements OtherTabBuilder, Runnable {
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
			// com.graham.passvaultplus.PvpContextUI.getActiveUI().notifyInfo("check thread 131");
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

	public synchronized void warning(final String s, final Exception e) {
			System.out.println("warning:" + Thread.currentThread().getName() + "| " + s);
		appendTimeStamp();
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

	public synchronized void info(String s) {
			System.out.println("info:" + Thread.currentThread().getName() + "| " + s);
		appendTimeStamp();
		log.append(s);
		log.append("\n");
		logUpdated();
	}

	private void logUpdated() {
		if (log.length() > 500000) {
			log = new StringBuilder("<<< log trimmed >>>" + log.substring(400000));
		}
		if (ta != null) {
			SwingUtilities.invokeLater(this); // call this.run()
		}
	}

	@Override
	public void run() {
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
			private LTRunner currentLt;
			public CommandExeCallBack() {
					super("Cancel");
			}
			Action oldAction;
				@Override
				public void taskStarting(LTRunner lt) {
						currentLt = lt;
						System.out.println("- - - CECB - - - taskStarting - - -");
						oldAction = doIt.getAction();
						doIt.setAction(this);
				}
				@Override
				public void taskComplete(LTRunner lt) {
						System.out.println("- - - CECB - - - taskComplete - - -");
						doIt.setAction(oldAction);
				}
				@Override
				public void handleException(LTRunner lt, Exception e) {
						System.out.println("- - - CECB - - - handleException - - -" + e.getMessage());
				}

				@Override
				public void actionPerformed(ActionEvent e) {
						currentLt.cancel();

				}
		}

		private void appendTimeStamp() {
			log.append(AppUtil.getMillisecondTimeStamp());
			log.append("|");
			log.append(Thread.currentThread().getName());
			log.append("| ");
		}

}
