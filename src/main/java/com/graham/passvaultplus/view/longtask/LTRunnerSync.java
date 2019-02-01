/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

import java.util.ArrayList;

/**
 * Handle the running of one LongTask.
 * For running syncronously - might show the Progress/Cancel dialog
 */
public class LTRunnerSync extends LTRunner {

		final private LongTask ltask;
		final private String taskDescription;

		private ArrayList<LTStep> steps = new ArrayList<>();
		private Exception ltaskException;
		private Thread mainThread;
		private volatile boolean wasCanceled;
		private long startTime;
		private boolean shouldShowCancelDialog;
		private LongTaskUI ltUi;

		public LTRunnerSync(LongTask t, String desc) {
				ltask = t;
				taskDescription = desc;
		}

		/**
		 * returns true if it was canceled by user
		 */
		public boolean runLongTask() throws Exception {
				System.out.println("LTRunnerSync.runLongTask.A");
				shouldShowCancelDialog = true;
				mainThread = Thread.currentThread();
				Thread ltaskThread = new Thread(this, "ltsy"); // Long Task Synchronous thread
				startTime = System.currentTimeMillis();
				ltaskThread.start();

				System.out.println("LTRunnerSync.runLongTask.B");

				while (getShouldShowCancelDialog()) {
						System.out.println("LTRunnerSync.runLongTask.C");
						try {
								Thread.sleep(LongTaskUI.SHOW_DELAY);
						} catch (InterruptedException e1) {
								System.out.println("LTRunnerSync.runLongTask.D - InterruptedException");
						}
						System.out.println("LTRunnerSync.runLongTask.E");
						if (LTManager.isWaitingUserInput()) {
								System.out.println("LTRunnerSync.runLongTask.F");
								waitUiDone();
						} else if (getShouldShowCancelDialog()) {
								ltUi = new LongTaskUI(taskDescription, steps, startTime, new CancelAction(this));
								if (getShouldShowCancelDialog() && !LTManager.isWaitingUserInput()) {
										synchronized (this) {
												//if (this.uiShowing && this.shouldShowCancelDialog) {
												mainThread = null; // set this to null because we dont want to have it intrupted anymore
												System.out.println("LTRunnerSync.runLongTask.G");
												//	}
										}
										ltUi.showCancelDialog();
								}
						} else {
								System.out.println("LTRunnerSync.runLongTask.H - should never be here 6482");// TODO we get here sometimes
						}
				}

				if (ltaskException != null) {
						throw ltaskException;
				}

				return wasCanceled;
		}

		@Override
		public void run() {
				try {
						LTManager.registerLTThread(this);
						System.out.println("LTRunnerSync.run.A");
						ltask.runLongTask();
						System.out.println("LTRunnerSync.run.B");
				} catch (LTCanceledException ltce) {
						System.out.println("LTRunnerSync.run.C - LTCanceledException");
				} catch (Exception e) {
						System.out.println("LTRunnerSync.run.D - Exception");
						ltaskException = e;
				} finally {
						killStuff(); // TODO should this be in finally ???
						LTManager.clearLTThread();
				}
		}

		synchronized void killStuff() {
				shouldShowCancelDialog = false;
				System.out.println("LTRunnerSync.killStuff.A");
				if (mainThread != null) {
						System.out.println("LTRunnerSync.killStuff.B - interruptMainThread");
						mainThread.interrupt();
				}
				if (ltUi != null) {
						ltUi.killStuff();
						ltUi = null;
				}
				System.out.println("LTRunnerSync.killStuff.C");
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

		synchronized boolean getShouldShowCancelDialog() {
				return shouldShowCancelDialog;
		}

		synchronized void nextStep(String stepDesc) throws LTCanceledException {
				if (wasCanceled) {
						throw new LTCanceledException();
				}

				// consider the last task done if it is running
				if (steps.size() > 0 && !steps.get(steps.size() - 1).done) {
						steps.get(steps.size() - 1).setDone();
						System.out.println("LTRunnerSync.nextStep.A - set last task done");
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

		/**
		 * @return True if it was canceled. False if the task cannot be canceled at this point.
		 */
		boolean setWasCanceled() {
				if (wasCanceled) {
						PvpContextUI.getActiveUI().notifyWarning("setWasCanceled called when wasCanceled = true");
				}
				if (ltask instanceof CancelableLongTask) {
						if (((CancelableLongTask)ltask).cancel()) {
								wasCanceled = true;
						}
				}
				return wasCanceled;
		}

		void setCannotCancel() {
				if (ltUi != null) {
						ltUi.setCannotCancel();
				}
		}

		void interruptForUserInputEnd() {
				if (mainThread != null) {
						mainThread.interrupt();
				}
		}

}
