/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

import java.awt.*;

public class LTManager {

		private static ThreadLocal<LTRunner> tlr = new ThreadLocal<>();

		/**
		 * This is non-zero if some user input UI is showing now. So that means don't make the cancel dialog visible - because we are waiting for user input.
		 */
		private static volatile int waitingUserInputId = 0;

		static public boolean isLTThread() {
			return Thread.currentThread().getName().equals(LTThread.THREAD_NAME);
		}

		static private LTRunner getLTThreadRunner() {
				Thread t = Thread.currentThread();
				if (t instanceof LTThread) {
						LTThread ltt = (LTThread) t;
						return ltt.rnr;
				}
				return null;
		}

		static public void registerCancelFunc(Runnable r) {
				Thread t = Thread.currentThread();
				if (t instanceof LTThread) {
						LTThread ltt = (LTThread)t;
						ltt.rnr.registerCancelCB(r);
				} else {
						PvpContextUI.getActiveUI().notifyWarning("LTManager.registerCancelFunc :: called on not LTThread");
				}
		}

		static public void unregisterCancelFunc(Runnable r) {
				Thread t = Thread.currentThread();
				if (t instanceof LTThread) {
						LTThread ltt = (LTThread)t;
						ltt.rnr.unregisterCancelCB(r);
				} else {
						PvpContextUI.getActiveUI().notifyWarning("LTManager.unregisterCancelFunc :: called on not LTThread");
				}
		}

		/**
		 * Run a LongTask asynchronous. Don't show a progress window.
		 */
		static public void run(LongTask lt) {
				run(lt, new LTCallbackDefaultImpl());
		}

		/**
		 * Run a LongTask asynchronous. Don't show a progress window.
		 */
		static public void run(LongTask lt, LTCallback cb) {
				if (cb == null) {
						throw new NullPointerException("LTCallback cb param cannot be null");
				}
				LTRunner ltr = getLTThreadRunner();
				if (ltr == null) {
						LTRunnerAsync r = new LTRunnerAsync(lt, cb);
						Thread ltThread = new Thread(r, LTThread.THREAD_NAME);
						ltThread.start();
				} else {
						try {
								cb.taskStarting(ltr);
								lt.runLongTask();
								cb.taskComplete(ltr);
						} catch (Exception e) {
								cb.handleException(ltr, e);
						}
				}
		}

		/**
		 * Run a LongTask asynchronous. Will return immediately if on the event thread. Show a progress window after 0.6 seconds.
		 */
		static public void runWithProgress(LongTask lt, String progressTitle) {
				runWithProgress(lt, progressTitle, null);
		}

		/**
		 * Run a LongTask asynchronous.  Will return immediately if on the event thread. Show a progress window after 0.6 seconds.
		 */
		static public void runWithProgress(LongTask lt, String progressTitle, LTCallback cb) {
				LTRunner ltr = getLTThreadRunner();
				if (ltr != null) {
						try {
								lt.runLongTask();
						} catch (Exception e) {
								if (cb != null) {
										cb.handleException(ltr, e);
								}
						}
				} else {
						LTRunnerSync r = new LTRunnerSync(lt, progressTitle, cb);
						r.runLongTask();
				}
		}

		static void registerLTThread(LTRunner r) {
				if (tlr.get() != null) {
						// we should not be here, because run and runWithProgress check for a LTRunner thread
						throw new RuntimeException("cannot start new LongTask when one is running on the current thread");
				}
				tlr.set(r);
		}

		static void clearLTThread() {
				tlr.remove();
		}

		/**
		 * Will throw a LTCanceledException if the Task was successfully canceled.
		 * Will assume the last step is completed if stepDone was not called.
		 */
		static public void nextStep(String stepDesc) throws LTCanceledException {
				LTRunner r = tlr.get();
				if (r == null) {
						PvpContextUI.getActiveUI().notifyWarning("Called nextStep when no LongTask active: " + stepDesc, new Exception());
				} else {
						r.nextStep(stepDesc);
				}
		}

		/**
		 * Will throw a LTCanceledException if the Task was successfully canceled.
		 * This does not need to be called. can just use nextStep.
		 */
		static public void stepDone(String stepDesc) throws LTCanceledException {
				LTRunner r = tlr.get();
				if (r == null) {
						PvpContextUI.getActiveUI().notifyWarning("Called stepDone when no LongTask active: " + stepDesc, new Exception());
				} else {
						r.stepDone(stepDesc);
				}
		}

		static public Window waitingUserInputStart() {
				return waitingUserInputStart(1);
		}

		static public void waitingUserInputEnd() {
				waitingUserInputEnd(1);
		}


		static public Window waitingUserInputStart(int id) {
				Window cancelW = null;
				if (waitingUserInputId == 0) {
						waitingUserInputId = id;
						for (Window w : Frame.getWindows()) {
								if (w instanceof LongTaskUI) {
										if (cancelW != null) {
												PvpContextUI.getActiveUI().notifyWarning("LTManager.waitingUserInputStart :: more than one LongTaskUI. This in not handled correctly");
										}
										cancelW = w;
										//System.out.println("waitingUserInputStart: " + ((LongTaskUI) w).getTitle());
								}
						}
				} else if (id >= waitingUserInputId) {
						PvpContextUI.getActiveUI().notifyWarning("LTManager.waitingUserInputStart :: " + id + " >= " + waitingUserInputId + " :: This should not happen. It means that a higher priority UI is nested in a lower priority one., or forgot to call waitingUserInputEnd", new Exception());
				}
				return cancelW;
		}

		static public void waitingUserInputEnd(int id) {
				if (id == waitingUserInputId) {
						waitingUserInputId = 0;
						if (tlr.get() != null) { // TODO should this be called on all LTRunners?
								tlr.get().interruptForUserInputEnd();
						}
				}
		}

		static public boolean isWaitingUserInput() {
				return waitingUserInputId != 0;
		}

}
