/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpContextUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

/**
 * Handle the UI of one LongTask
 */
public class LongTaskUI  {

		static long SHOW_DELAY = 800;
		static long MIN_SHOW_TIME = 500;

		private JDialog cancelDialog;
		private JTextArea progressText;
		private JLabel timerLabel;
		private Thread timerThread;
		private JButton cancelButton;
		private long startTime;

		public LongTaskUI(String taskDescription, ArrayList<LTStep> steps, long startTimeParam, CancelAction cancelA) {
				System.out.println("LongTaskUI.LongTaskUI.A");
				startTime = startTimeParam;
				cancelDialog = new JDialog(PvpContextUI.getActiveUI().getFrame(), "Pass Vault Plus (AM)", Dialog.ModalityType.APPLICATION_MODAL);
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

						progressText.setPreferredSize(new Dimension(300, 68));
						progressText.setEditable(false);
						progressText.setFont(BCUtil.getBodyFont());
						progressText.setBackground(p.getBackground());

						stepsChanged(steps);
						p.add(progressText);
						centerPanel.add(p);

						p = new JPanel(new FlowLayout(FlowLayout.LEFT));
						timerLabel = new JLabel("Total Time: 0");
						p.add(timerLabel);
						centerPanel.add(p);
				}

				{
						final JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
						cancelButton = new JButton(cancelA);
						p.add(cancelButton);
						p.setBorder(new EmptyBorder(4, 20, 10, 16));
						//p.setBorder(new MatteBorder(4,20,10,16, Color.MAGENTA));

						centerPanel.add(p);
				}

				timerThread = new Thread(() -> {
						try {
								while (true) {
										Thread.sleep(1000);
										updateTimerDisplay();
								}
						} catch (InterruptedException ie) {
								//System.out.println("at Timer Thread InterruptedException 1");
						}
						//System.out.println("LongTaskUI.LongTaskUI.B - at 99 Timer Thread done");
				}, "LongTaskUITimerThread");
				timerThread.start();

				cancelDialog.pack();
				//	BCUtil.center(cancelDialog, PvpContext.getActiveUI().getFrame());
				cancelDialog.setLocationRelativeTo(PvpContextUI.getActiveUI().getFrame());
				cancelDialog.setResizable(false);
				System.out.println("LongTaskUI.LongTaskUI.C");

				cancelDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}

		void stepsChanged(ArrayList<LTStep> steps) {
				if (progressText != null) {
					StringBuffer sb = new StringBuffer();
					for (int i = Math.max(steps.size() - 5, 0); i < steps.size(); i++) {
							sb.append(steps.get(i));
							sb.append("\n");
					}
				//	System.out.println("LongTaskUI.stepsChanged.A - " + sb.toString());
					progressText.setText(sb.toString());
				}
		}

		synchronized void killStuff() {
				timerThread.interrupt();
				long showTime = System.currentTimeMillis() - startTime - SHOW_DELAY;
				System.out.println("LongTaskUI.showTime.A - " + showTime);
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

		void setCannotCancel() {
				cancelButton.setEnabled(false);
				cancelButton.setText("Can't Cancel");
				cancelButton.setToolTipText("The task cannot be canceled anymore. It is too far along.");
		}

		void showCancelDialog() {
				cancelDialog.setVisible(true); // this is the line that causes the dialog to Block
		}

		private void updateTimerDisplay() {
				//System.out.println("LongTaskUI.updateTimerDisplay.A");
				timerLabel.setText("Total Time: " + Integer.toString((int)((System.currentTimeMillis() - startTime) / 1000)));
		}

}
