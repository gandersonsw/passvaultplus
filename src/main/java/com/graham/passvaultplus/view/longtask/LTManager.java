/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

public class LTManager {

		private static ThreadLocal<LTRunner> tlr = new ThreadLocal<>();

		/**
		 * This is non-zero if some user input UI is showing now. So that means don't make the cancel dialog visible - because we are waiting for user input.
		 */
		private static volatile int waitingUserInputId = 0;

		static public boolean isLTThread() {
			return Thread.currentThread().getName().equals("pvplt"); // TODO make const
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
				run(lt, null);
		}

		/**
		 * Run a LongTask asynchronous. Don't show a progress window.
		 */
		static public void run(LongTask lt, LTCallback cb) {
				LTRunner ltr = getLTThreadRunner();
				if (ltr == null) {
						System.out.println("LTManager.run.A isLTThread=false");
						LTRunnerAsync r = new LTRunnerAsync(lt, cb);
						Thread ltThread = new Thread(r, "pvplt"); // // Pvp Long Task // TODO make const
						ltThread.start();
				} else {
						System.out.println("LTManager.run.A isLTThread=true");
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
		 * Run a LongTask asynchronous. Show a progress window after 0.6 seconds.
		 */
		static public void runWithProgress(LongTask lt, String progressTitle) {
				runWithProgress(lt, progressTitle, null);
		}

		/**
		 * Run a LongTask asynchronous. Show a progress window after 0.6 seconds.
		 */
		static public void runWithProgress(LongTask lt, String progressTitle, LTCallback cb) {
				LTRunner ltr = getLTThreadRunner();
				if (ltr != null) {
						System.out.println("LTManager.runWithProgress.A isLTThread=true");
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

		static public void waitingUserInputStart() {
				waitingUserInputStart(1);
		}

		static public void waitingUserInputEnd() {
				waitingUserInputEnd(1);
		}


		static public void waitingUserInputStart(int id) {
				System.out.println("LTManager.waitingUserInputStart: " + id + " > " + waitingUserInputId);
				if (id > waitingUserInputId) {
						waitingUserInputId = id;
				}
		}

		static public void waitingUserInputEnd(int id) {
				System.out.println("LTManager.waitingUserInputEnd: " + id + " == " + waitingUserInputId);
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
