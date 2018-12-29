/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LongTaskUI implements Runnable {

		private JDialog cancelDialog;
		private JLabel progressLabel;
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

		public LongTaskUI(LongTask t, String desc) {
				ltask = t;
				taskDescription = desc;
		}

		/**
		 * returns true if it was canceled by user
		 */
		public boolean runLongTask() throws Exception {
				System.out.println("at 1 runLongTask");
				shouldShowCancelDialog = true;
				mainThread = Thread.currentThread();
				ltaskThread = new Thread(this, "LongTaskUI Thread");
				startTime = System.currentTimeMillis();
				ltaskThread.start();

				System.out.println("at 2 runLongTask");
				try {
					Thread.sleep(800);
				} catch (InterruptedException e1) {
						System.out.println("at runLongTask InterruptedException 1");
				}
				synchronized(this) {
						mainThread = null;
				}
				if (getShouldShowCancelDialog()) {
						showCancelDialog();
				}

				if (ltaskException != null) {
						throw ltaskException;
				}

				return wasCanceled;
		}

		public void nextStep(String stepName) {
				progressLabel.setText(stepName);
		}

		@Override
		public void run() {
				try {
						System.out.println("at 3 run");
						ltask.runLongTask(this);
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
						cancelDialog.setVisible(false);
						cancelDialog = null;
				}
				System.out.println("at 21 interruptMainThread");
		}

		public void showCancelDialog() {
				System.out.println("at 8 ShowUITimer");
				cancelDialog = new JDialog(PvpContext.getActiveUI().getFrame(), "Pass Vault Plus", Dialog.ModalityType.APPLICATION_MODAL);
				cancelDialog.getContentPane().setLayout(new BorderLayout());

				ImageIcon icn = PvpContext.getIcon("option-pane-info", PvpContext.OPT_ICN_SCALE);
				JLabel icnLab = new JLabel(icn);
				icnLab.setBorder(new EmptyBorder(16,25,16,24));
				cancelDialog.getContentPane().add(icnLab, BorderLayout.WEST);

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
						progressLabel = new JLabel("");
						p.add(progressLabel);
						centerPanel.add(p);

						p = new JPanel(new FlowLayout(FlowLayout.LEFT));
						timerLabel = new JLabel("0");
						p.add(timerLabel);
						centerPanel.add(p);
				}

				{
						final JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
						p.add(new JButton(new CancelAction()));
						p.setBorder(new EmptyBorder(20,20,20,20));
						centerPanel.add(p);
				}

				timerThread = new Thread(() -> { try { while (true) { Thread.sleep(1000); updateTimerDisplay();} } catch (InterruptedException ie) { System.out.println("at Timer Thread InterruptedException 1"); } System.out.println("at 99 Timer Thread done"); }, "LongTaskUI Timer Thread");
				timerThread.start();

				cancelDialog.pack();
				BCUtil.center(cancelDialog, PvpContext.getActiveUI().getFrame());
				cancelDialog.setResizable(false);
				System.out.println("at 9 ShowUITimer");

				cancelDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

				cancelDialog.setVisible(true); // this is the line that causes the dialog to Block
		}

		void updateTimerDisplay() {
				System.out.println("at 10 updateTimerDisplay");
				timerLabel.setText(Integer.toString((int)((System.currentTimeMillis() - startTime) / 1000)));
		}

		class CancelAction extends AbstractAction {
				CancelAction() {
						super("Cancel");
				}
				public void actionPerformed(ActionEvent e) {
						System.out.println("at 11 CancelAction");
						wasCanceled = true;
						ltask.cancel(LongTaskUI.this);
						killStuff();
				}
		}

}
