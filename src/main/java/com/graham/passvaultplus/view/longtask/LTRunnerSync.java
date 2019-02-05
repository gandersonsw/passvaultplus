/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

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

		private LongTaskUI ltUi;

		public LTRunnerSync(LongTask t, String title, LTCallback cb) {
				ltask = t;
				taskTitle = title;
				ltCb = cb == null ? new LTCallbackDefaultImpl() : cb;
		}

		public void runLongTask() {
				syncManager = new LTSyncManager(this);
				//smThread = new Thread(syncManager, "ltsm"); // LongTask Sync Manager


				Thread ltaskThread = new LTThread(this); // Pvp Long Task
				startTime = System.currentTimeMillis();
				ltaskThread.start(); // This is the task that takes a long time
				//smThread.start(); // this is the thread that manages the UI and other stuff

				// TEST - run this on the current thread
				parentThread = Thread.currentThread(); // in this case, this is the Swing Event Thread
				syncManager.run(); // run it on the event thread
		}

		@Override
		public void run() {
				try {
						LTManager.registerLTThread(this);
						//System.out.println("LTRunnerSync.run.A");
						ltask.runLongTask();
						//System.out.println("LTRunnerSync.run.B");
				} catch (LTCanceledException ltce) {
						System.out.println("LTRunnerSync.run.C - LTCanceledException");
				} catch (Exception e) {
						System.out.println("LTRunnerSync.run.D - Exception");
						ltCb.handleException(this, e);
				} finally {
						killStuff(); // TODO should this be in finally ???
						LTManager.clearLTThread();
				}
		}

		@Override
		synchronized void killStuff() {
				syncManager.setShouldShowCancelDialog(false);
			//	shouldShowCancelDialog = false;
				System.out.println("LTRunnerSync.killStuff.A");
				if (parentThread != null) {
						System.out.println("LTRunnerSync.killStuff.B - interruptMainThread");
						parentThread.interrupt();
				}
				if (ltUi != null) {
						ltUi.killStuff();
						ltUi = null;
				}
				System.out.println("LTRunnerSync.killStuff.C");
		}

		synchronized void nextStep(String stepDesc) throws LTCanceledException {
				if (wasCanceled) {
						throw new LTCanceledException();
				}

				// consider the last task done if it is running
				if (steps.size() > 0 && !steps.get(steps.size() - 1).done) {
						steps.get(steps.size() - 1).setDone();
						//System.out.println("LTRunnerSync.nextStep.A - set last task done");
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
				// TODO does this need to be syncronized? this is causing a deadlock
				ltUi = new LongTaskUI(taskTitle, steps, startTime, new CancelAction(LTRunnerSync.this));
				//	if (getShouldShowCancelDialog() && !LTManager.isWaitingUserInput()) {
				//		synchronized (this) {
				//if (this.uiShowing && this.shouldShowCancelDialog) {
				parentThread = null; // set this to null because we dont want to have it intrupted anymore
				System.out.println("LTSyncManager.showCancelDialog.G");
				//	}
				//			}
				ltUi.showCancelDialog();
				System.out.println("LTSyncManager.showCancelDialog.H");
				//	}
		}

}

class LTSyncManager implements Runnable {

		private boolean shouldShowCancelDialog = true;
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
						System.out.println("- - - - - LTSyncManager.run.A");
						try {
								Thread.sleep(LongTaskUI.SHOW_DELAY);
						} catch (InterruptedException e1) {
								System.out.println("- - - - - LTSyncManager.run.D - InterruptedException");
						}
						if (LTManager.isWaitingUserInput()) {
								waitUiDone();
						} else if (getShouldShowCancelDialog()) {
							ltr.showCancelDialog();
						} else {
								System.out.println("- - - - - LTSyncManager.run.I - should never be here 6482");// TODO we get here sometimes
						}
				}
				System.out.println("- - - - - LTSyncManager.run.Z");
		}

		private void waitUiDone() {
				while (LTManager.isWaitingUserInput()) {
						try {
								Thread.sleep(30000);
								System.out.println("LTRunnerSync.waitUiDone.A");
						} catch (InterruptedException e1) {
								System.out.println("LTRunnerSync.waitUiDone.B - InterruptedException");
						}
				}
		}
}