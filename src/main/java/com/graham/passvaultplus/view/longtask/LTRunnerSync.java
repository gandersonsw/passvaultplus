/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

import java.util.ArrayList;

/**
 * Handle the running of one LongTask.
 * For running syncronously - might show the Progress/Cancel dialog
 */
public class LTRunnerSync extends LTRunner {

		private ArrayList<LTStep> steps = new ArrayList<>();
		private Exception ltaskException;
		private Thread ltaskThread;
		private Thread mainThread;
		private boolean wasCanceled;
		private long startTime;
		private boolean stillRinning;
		private LongTask ltask;
		private String taskDescription;
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
				try {
						stillRinning = true;
						System.out.println("LTRunnerSync.runLongTask.A");
						shouldShowCancelDialog = true;
						mainThread = Thread.currentThread();
						ltaskThread = new Thread(this, "LTRunnerSyncThread");
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
				} finally {
						stillRinning = false;
				}
		}

		@Override
		public void run() {
				try {
						LTManager.registerSyncThread(this);
						System.out.println("LTRunnerSync.run.A");
						ltask.runLongTask();
						System.out.println("LTRunnerSync.run.B");
				} catch (Exception e) {
						System.out.println("LTRunnerSync.run.C - Exception");
						ltaskException = e;
				} finally {
						killStuff(); // TODO should this be in finally ???
						LTManager.clearSyncThread();
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

		synchronized void nextStep(String stepDesc) {
				//	synchronized (activeTask) {
				if (stillRinning) {
						steps.add(new LTStep(stepDesc));
						if (ltUi != null) {
								ltUi.stepsChanged(steps);
						}
				}
				//	}
		}

		synchronized void stepDone(String stepDesc) {
				//	synchronized (activeTask) {
				if (stillRinning) {
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
				//	}
		}

		void setWasCanceled() {
				if (wasCanceled) {
						PvpContextUI.getActiveUI().notifyWarning("setWasCanceled called when wasCanceled = true");
				}
				if (ltask instanceof CancelableLongTask) {
						((CancelableLongTask)ltask).cancel();
				}
				wasCanceled = true;
		}

		void interruptForUserInputEnd() {
				if (mainThread != null) {
						mainThread.interrupt();
				}
		}

}
