/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class LongTaskUI implements Runnable {

		private static long SHOW_DELAY = 800;
		private static long MIN_SHOW_TIME = 500;

		static class TaskStep {
				String description;
				long runtime;
				boolean done;
				public TaskStep(String d) {
						description = d;
						runtime = System.currentTimeMillis();
				}
				public void setDone() {
						done = true;
						runtime = System.currentTimeMillis() - runtime;
				}
				@Override
				public String toString() {
						if (done) {
								return description + " (" + (runtime / 1000) + "s)";
						} else {
								return description + "...";
						}
				}
		}

		private static LongTaskUI activeTask; // there can be only one at a time, if there is already one, don't create a new one

		private JDialog cancelDialog;
		private JTextArea progressText;
		private JLabel timerLabel;
		private LongTask ltask;
		private Exception ltaskException;
		private Thread ltaskThread;
		private Thread mainThread;
		private Thread timerThread;
		private boolean wasCanceled;
		private long startTime;
		private boolean shouldShowCancelDialog;
		private String taskDescription;
		private boolean stillRinning;
		private JButton cancelButton;

		private ArrayList<TaskStep> steps = new ArrayList<>();

		public LongTaskUI(LongTask t, String desc) {
				ltask = t;
				taskDescription = desc;
		}

		/**
		 * returns true if it was canceled by user
		 */
		public boolean runLongTask() throws Exception {
				if (activeTask != null && activeTask.stillRinning) {
						nextStep(taskDescription);
						ltask.runLongTask();
						stepDone(taskDescription);
						return activeTask.wasCanceled;
				}
				try {
						stillRinning = true;
						activeTask = this;
						System.out.println("at 1 runLongTask");
						shouldShowCancelDialog = true;
						mainThread = Thread.currentThread();
						ltaskThread = new Thread(this, "LongTaskUI Thread");
						startTime = System.currentTimeMillis();
						ltaskThread.start();

						System.out.println("at 2 runLongTask");
						try {
								Thread.sleep(SHOW_DELAY);
						}
						catch (InterruptedException e1) {
								System.out.println("at runLongTask InterruptedException 1");
						}
						synchronized (this) {
								mainThread = null;
						}
						if (getShouldShowCancelDialog()) {
								showCancelDialog();
						}

						if (ltaskException != null) {
								throw ltaskException;
						}

						return wasCanceled;
				} finally {
						stillRinning = false;
				}
		}

		static public void nextStep(String stepDesc) {
				if (activeTask != null) {
						synchronized (activeTask) {
						if (activeTask.stillRinning) {
								activeTask.steps.add(new TaskStep(stepDesc));
								activeTask.stepsChanged();
						}
					}
				}
		}

		static public void stepDone(String stepDesc) {
				if (activeTask != null) {
						synchronized (activeTask) {
								if (activeTask.stillRinning) {
										for (int i = activeTask.steps.size() - 1; i >= 0; i--) {
												if (activeTask.steps.get(i).description.equals(stepDesc) && !activeTask.steps.get(i).done) {
														activeTask.steps.get(i).setDone();
														activeTask.stepsChanged();
														return;
												}
										}
								}
						}
				}
		}

		private void stepsChanged() {
				if (progressText != null) {
					StringBuffer sb = new StringBuffer();
					for (int i = Math.max(steps.size() - 5, 0); i < steps.size(); i++) {
							sb.append(steps.get(i));
							sb.append("\n");
					}
					System.out.println("at 8999:" + sb.toString());
						progressText.setText(sb.toString());
				}
		}

		@Override
		public void run() {
				try {
						System.out.println("at 3 run");
						ltask.runLongTask();
						killStuff();
						System.out.println("at 4 run");
				} catch (Exception e) {
						System.out.println("at 5 run");
						ltaskException = e;
				}
		}

		synchronized boolean getShouldShowCancelDialog() {
				return shouldShowCancelDialog;
		}

		synchronized void killStuff() {
				shouldShowCancelDialog = false;
				System.out.println("at 6 interruptMainThread");
				if (mainThread != null) {
						System.out.println("at 7 interruptMainThread");
						mainThread.interrupt();
				}
				if (cancelDialog != null) {
						timerThread.interrupt();
						long showTime = System.currentTimeMillis() - startTime - SHOW_DELAY;
						System.out.println("killStuff: showTime:" + showTime);
						if (showTime < MIN_SHOW_TIME) {
								try {
										cancelButton.setEnabled(false);
										cancelButton.setText("Done");
										Thread.sleep(MIN_SHOW_TIME - showTime);
								} catch (InterruptedException e) { }
						}
						cancelDialog.setVisible(false);
						cancelDialog = null;
				}
				System.out.println("at 21 interruptMainThread");
		}

		private void showCancelDialog() {
				System.out.println("at 8 ShowUITimer");
				cancelDialog = new JDialog(PvpContext.getActiveUI().getFrame(), "Pass Vault Plus", Dialog.ModalityType.APPLICATION_MODAL);
				cancelDialog.getContentPane().setLayout(new BorderLayout());

				{
						ImageIcon icn = PvpContext.getIcon("option-pane-info", PvpContext.OPT_ICN_SCALE);
						JLabel icnLab = new JLabel(icn);
						icnLab.setBorder(new EmptyBorder(16, 25, 16, 24));
						JPanel p = new JPanel(new BorderLayout());
						p.add(icnLab, BorderLayout.NORTH);
						cancelDialog.getContentPane().add(p, BorderLayout.WEST);
				}

				final JPanel centerPanel = new JPanel();
				centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
				cancelDialog.getContentPane().add(centerPanel, BorderLayout.CENTER);

				{
						JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
						p.add(new JLabel(""));
						centerPanel.add(p);
				}
				{
						JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
						p.add(new JLabel(taskDescription));
						centerPanel.add(p);

						p = new JPanel(new FlowLayout(FlowLayout.LEFT));
						progressText = new JTextArea();

						progressText.setPreferredSize(new Dimension(300,68));
						progressText.setEditable(false);
						progressText.setFont(BCUtil.getBodyFont());
						progressText.setBackground(p.getBackground());


						stepsChanged();
						p.add(progressText);
						centerPanel.add(p);

						p = new JPanel(new FlowLayout(FlowLayout.LEFT));
						timerLabel = new JLabel("Total Time: 0");
						p.add(timerLabel);
						centerPanel.add(p);
				}

				{
						final JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
						cancelButton = new JButton(new CancelAction());
						p.add(cancelButton);
						p.setBorder(new EmptyBorder(4,20,10,16));
						//p.setBorder(new MatteBorder(4,20,10,16, Color.MAGENTA));

						centerPanel.add(p);
				}

				timerThread = new Thread(() -> { try { while (true) { Thread.sleep(1000); updateTimerDisplay();} } catch (InterruptedException ie) { System.out.println("at Timer Thread InterruptedException 1"); } System.out.println("at 99 Timer Thread done"); }, "LongTaskUI Timer Thread");
				timerThread.start();

				cancelDialog.pack();
			//	BCUtil.center(cancelDialog, PvpContext.getActiveUI().getFrame());
				cancelDialog.setLocationRelativeTo(PvpContext.getActiveUI().getFrame());
				cancelDialog.setResizable(false);
				System.out.println("at 9 ShowUITimer");

				cancelDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

				cancelDialog.setVisible(true); // this is the line that causes the dialog to Block
		}

		private void updateTimerDisplay() {
				System.out.println("at 10 updateTimerDisplay");
				timerLabel.setText("Total Time: " + Integer.toString((int)((System.currentTimeMillis() - startTime) / 1000)));
		}

		class CancelAction extends AbstractAction {
				CancelAction() {
						super("Cancel");
				}
				public void actionPerformed(ActionEvent e) {
						System.out.println("at 11 CancelAction");
						wasCanceled = true;
						ltask.cancel();
						killStuff();
				}
		}

}
