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
public class LongTaskUI extends JDialog {

		static long SHOW_DELAY = 600;
		static long MIN_SHOW_TIME = 500;

		//private JDialog cancelDialog;
		private JTextArea progressText;
		private JLabel timerLabel;
		private Thread timerThread;
		private JButton cancelButton;
		private long startTime;

		public LongTaskUI(String taskTitle, ArrayList<LTStep> steps, long startTimeParam, CancelAction cancelA) {
				super(PvpContextUI.getActiveUI().getFrame(), "Pass Vault Plus (AM)", Dialog.ModalityType.APPLICATION_MODAL);
				com.graham.passvaultplus.PvpContextUI.checkEvtThread("3901");
				startTime = startTimeParam;
				//cancelDialog = new JDialog(PvpContextUI.getActiveUI().getFrame(), "Pass Vault Plus (AM)", Dialog.ModalityType.APPLICATION_MODAL);
				getContentPane().setLayout(new BorderLayout());

				{
						ImageIcon icn = PvpContext.getIcon("option-pane-info", PvpContext.OPT_ICN_SCALE);
						JLabel icnLab = new JLabel(icn);
						icnLab.setBorder(new EmptyBorder(16, 25, 16, 24));
						JPanel p = new JPanel(new BorderLayout());
						p.add(icnLab, BorderLayout.NORTH);
						getContentPane().add(p, BorderLayout.WEST);
				}

				final JPanel centerPanel = new JPanel();
				centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
				getContentPane().add(centerPanel, BorderLayout.CENTER);

				{
						JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
						p.add(new JLabel(""));
						centerPanel.add(p);
				}
				{
						JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
						p.add(new JLabel(taskTitle));
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
						} catch (InterruptedException ie) { }
						//System.out.println("LongTaskUI.LongTaskUI.B - at 99 Timer Thread done");
				}, "tuit"); // task UI thread
				timerThread.start();

				pack();
				setLocationRelativeTo(PvpContextUI.getActiveUI().getFrame());
				setResizable(false);
				setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}

		void stepsChanged(ArrayList<LTStep> steps) {
				if (progressText != null) {
					StringBuffer sb = new StringBuffer();
					for (int i = Math.max(steps.size() - 5, 0); i < steps.size(); i++) {
							sb.append(steps.get(i));
							sb.append("\n");
					}
					progressText.setText(sb.toString());
				}
		}

		synchronized void killStuff() {
				timerThread.interrupt();
				long showTime = System.currentTimeMillis() - startTime - SHOW_DELAY;
				if (showTime < MIN_SHOW_TIME) {
						// TODO test this
						try {
								cancelButton.setEnabled(false);
								cancelButton.setText("Done");
								Thread.sleep(MIN_SHOW_TIME - showTime);
						} catch (InterruptedException e) { }
				}
				setVisible(false);
				//cancelDialog = null;
		}

		void setCanCancel(boolean cc) {
				PvpContextUI.checkEvtThread("5473");
				cancelButton.setEnabled(cc);
				cancelButton.setText(cc ? "Cancel" : "Can't Cancel");
		}

		void showCancelDialog() {
				com.graham.passvaultplus.PvpContextUI.checkEvtThread("3902");
				setVisible(true); // this is the line that causes the dialog to Block
		}

		private void updateTimerDisplay() {
				timerLabel.setText("Total Time: " + Integer.toString((int)((System.currentTimeMillis() - startTime) / 1000)));
		}

}
