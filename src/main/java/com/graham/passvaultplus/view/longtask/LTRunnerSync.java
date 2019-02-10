/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Handle the running of one LongTask.
 * For running syncronously, meaning UI can't continue until this is done.
 * Might show the Progress/Cancel dialog
 */
public class LTRunnerSync extends LTRunner {

		final private LongTask ltask;
		final private String taskTitle;
		final private LTCallback ltCb;

		private ArrayList<LTStep> steps = new ArrayList<>();
		private Thread parentThread;
		private long startTime;
		private LTSyncManager syncManager;
		private volatile LongTaskUI ltUi;

		public LTRunnerSync(LongTask t, String title, LTCallback cb) {
				ltask = t;
				taskTitle = title;
				ltCb = cb == null ? new LTCallbackDefaultImpl() : cb;
		}

		public void runLongTask() {
		//		new Exception("runLongTask start").printStackTrace();
				syncManager = new LTSyncManager(this);

				Thread ltaskThread = new LTThread(this); // Pvp Long Task
				startTime = System.currentTimeMillis();
				ltaskThread.start(); // This is the task that takes a long time

				if (SwingUtilities.isEventDispatchThread()) {
						PvpContextUI.getActiveUI().notifyWarning("LTRunnerSync.runLongTask.A :: WARNING - starting new thread", new Exception());
						parentThread = new Thread(syncManager, "ltsm");
						parentThread.start();
						//SwingUtilities.invokeLater(syncManager);
				} else {
						parentThread = Thread.currentThread(); // in this case, this is the Swing Event Thread
						syncManager.run(); // run it on the current thread
				}
		}

		@Override
		public void run() {
				try {
						LTManager.registerLTThread(this);
						ltask.runLongTask();
				} catch (LTCanceledException ltce) {
						System.out.println("LTRunnerSync.run.C - LTCanceledException");
				} catch (Exception e) {
						System.out.println("LTRunnerSync.run.D - Exception");
						ltCb.handleException(this, e);
				} finally {
						killStuff();
						LTManager.clearLTThread();
				}
		}

		@Override
		synchronized void killStuff() {
				syncManager.setShouldShowCancelDialog(false);
				if (parentThread != null) {
						System.out.println("LTRunnerSync.killStuff.B - interruptMainThread");
						parentThread.interrupt();
				}
				if (ltUi != null) {
						final LongTaskUI ltUiCopy = ltUi;
						SwingUtilities.invokeLater(() -> ltUiCopy.killStuff());
						ltUi = null;
				}
		}

		synchronized void nextStep(String stepDesc) throws LTCanceledException {
				if (wasCanceled) {
						throw new LTCanceledException();
				}

				// consider the last task done if it is running
				if (steps.size() > 0 && !steps.get(steps.size() - 1).done) {
						steps.get(steps.size() - 1).setDone();
				}
				steps.add(new LTStep(stepDesc));
				if (ltUi != null) {
						ltUi.stepsChanged(steps);
				}
		}

		synchronized void stepDone(String stepDesc) throws LTCanceledException {
				if (wasCanceled) {
						throw new LTCanceledException();
				}

				for (int i = steps.size() - 1; i >= 0; i--) {
						if (steps.get(i).description.equals(stepDesc) && !steps.get(i).done) {
								steps.get(i).setDone();
								if (ltUi != null) {
										ltUi.stepsChanged(steps);
								}
								return;
						}
				}
		}

		void registerCancelCB(Runnable r) {
				super.registerCancelCB(r);
				if (ltUi != null) {
						ltUi.setCanCancel(canCancelNow());
				}
		}

		void unregisterCancelCB(Runnable r) {
				super.unregisterCancelCB(r);
				if (ltUi != null) {
						ltUi.setCanCancel(canCancelNow());
				}
		}

		void interruptForUserInputEnd() {
				if (parentThread != null) {
						parentThread.interrupt();
				}
		}

		void showCancelDialog() {
				if (syncManager.getShouldShowCancelDialog() && !LTManager.isWaitingUserInput() && ltUi == null) {
						final LongTaskUI ltUiCopy;
						synchronized(this) {
								ltUi = new LongTaskUI(taskTitle, steps, startTime, new CancelAction(LTRunnerSync.this));
								ltUiCopy = ltUi;
								parentThread = null; // set this to null because we dont want to have it intrupted anymore
						}
						// we already showed it - don't try to show it again
						if (syncManager.getShouldShowCancelDialog()) {
								ltUiCopy.showCancelDialog();
						}
				}
		}

}

class LTSyncManager implements Runnable {

		private volatile boolean shouldShowCancelDialog = true;
		private LTRunnerSync ltr;

		public LTSyncManager(LTRunnerSync ltrParam) {
				ltr = ltrParam;
		}

		boolean getShouldShowCancelDialog() {
				return shouldShowCancelDialog;
		}

		void setShouldShowCancelDialog(boolean b) {
				shouldShowCancelDialog = b;
		}

		@Override
		public void run() {
				while (getShouldShowCancelDialog()) {
						try {
								Thread.sleep(LongTaskUI.SHOW_DELAY);
						} catch (InterruptedException e1) {
								System.out.println("- - - - - LTSyncManager.run.D - InterruptedException");
						}
						if (LTManager.isWaitingUserInput()) {
								waitUiDone();
						} else if (getShouldShowCancelDialog()) {
								SwingUtilities.invokeLater(() -> ltr.showCancelDialog());

						}
				}
		}

		private void waitUiDone() {
				while (LTManager.isWaitingUserInput()) {
						try {
								Thread.sleep(10000);
								System.out.println("LTRunnerSync.waitUiDone.A");
						} catch (InterruptedException e1) {
								System.out.println("LTRunnerSync.waitUiDone.B - InterruptedException");
						}
				}
		}
}