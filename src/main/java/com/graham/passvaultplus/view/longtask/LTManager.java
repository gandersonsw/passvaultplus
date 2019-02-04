/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

public class LTManager {

		private static ThreadLocal<LTRunner> tlr = new ThreadLocal<>();

		/**
		 * This is true if some user input UI is showing now. So that means don't make the cancel dialog visible - because we are waiting for user input.
		 */
	//	private static volatile boolean waitingUserInput = false;
		private static volatile int waitingUserInputId = 0;

		static public boolean isLTThread() {
			return Thread.currentThread().getName().equals("pvplt"); // TODO make const
		}
/*
		static public LTRunner getLTRunner() {
				Thread t = Thread.currentThread();
				if (t instanceof LTThread) {
						LTThread ltt = (LTThread)t;
						return ltt.rnr;
				}
				return null;
		}*/

		static public void registerCancelFunc(Runnable r) {
				Thread t = Thread.currentThread();
				if (t instanceof LTThread) {
						LTThread ltt = (LTThread)t;
						ltt.rnr.registerCancelCB(r);
				}
		}

		static public void unregisterCancelFunc(Runnable r) {
				Thread t = Thread.currentThread();
				if (t instanceof LTThread) {
						LTThread ltt = (LTThread)t;
						ltt.rnr.unregisterCancelCB(r);
				}
		}

		/**
		 * Run a LongTask asynchronous. Don't allow cancel, and don't show a progress window.
		 */
		static public void run(LongTask lt) {
				run(lt, null);
		}

		/**
		 * Run a LongTask asynchronous. Don't allow cancel, and don't show a progress window.
		 */
		static public void run(LongTask lt, LTCallback cb) {
				if (isLTThread()) {
						System.out.println("LTManager.run.A isLTThread=true");
						try {
								cb.taskStarting(new LTRunnerAsync(lt, cb)); // TODO
								lt.runLongTask();
								cb.taskComplete(new LTRunnerAsync(lt, cb)); // TODO
						} catch (Exception e) {
								cb.handleException(new LTRunnerAsync(lt, cb), e); // TODO
						}
				} else {
						System.out.println("LTManager.run.A isLTThread=false");
						LTRunnerAsync r = new LTRunnerAsync(lt, cb);
						Thread ltThread = new Thread(r, "pvplt"); // // Pvp Long Task // TODO make const
						ltThread.start();
				}
		}

		/**
		 * Run a LongTask asynchronous. Show a progress window after 0.5 seconds, and allow cancel.
		 */
		static public void runWithProgress(LongTask lt, String progressTitle) {
				runWithProgress(lt, progressTitle, null);
		}

		/**
		 * Run a LongTask asynchronous. Show a progress window after 0.5 seconds, and allow cancel.
		 */
		static public void runWithProgress(LongTask lt, String progressTitle, LTCallback cb) {
				if (isLTThread()) {
						try {
								lt.runLongTask();
						} catch (Exception e) {
								if (cb != null) {
										cb.handleException(new LTRunnerAsync(lt, cb), e); // TODO
								}
						}
				} else {
						LTRunnerSync r = new LTRunnerSync(lt, progressTitle, cb);
						r.runLongTask();
				}
		}

		static void registerLTThread(LTRunner r) {
				if (tlr.get() != null) {
						// TODO this is not true
						throw new RuntimeException("cannot start new LongTask when one is running on the current thread");
				}
				tlr.set(r);
		}

		static void clearLTThread() {
				tlr.remove();
		}

	/*
		static public void runSync(LongTask lt, String desc) throws Exception {
				LTRunnerSync r = new LTRunnerSync(lt, desc);
				r.runLongTask();
		}


		static public void runSync(LongTaskNoException lt, String desc) {
				LTRunnerSync r = new LTRunnerSync(lt, desc);
				try {
						r.runLongTask();
				} catch (Exception e) {
						// There should not be an exception
						PvpContextUI.getActiveUI().notifyWarning("runSync had unexpected exception", e);
				}
		}


		static public boolean runSync(CancelableLongTask lt, String desc) throws Exception {
				LTRunnerSync r = new LTRunnerSync(lt, desc);
				return r.runLongTask();
		}


		static public boolean runSync(CancelableLongTaskNoEception lt, String desc) {
				LTRunnerSync r = new LTRunnerSync(lt, desc);
				boolean ret = true;
				try {
						ret = r.runLongTask();
				} catch (Exception e) {
						// There should not be an exception
						PvpContextUI.getActiveUI().notifyWarning("runSync had unexpected exception", e);
				}
				return ret;
		}
		*/

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

		static public void waitingUserInputStart() { // waitingUserInput
				waitingUserInputStart(1);
			//	System.out.println("LTManager.waitingUserInputStart");
			///	if (waitingUserInputId != 0) {
			//			PvpContextUI.getActiveUI().notifyWarning("LTManager.waitingUserInputStart waitingUserInput is : " + waitingUserInputId);
			//	}
			//	waitingUserInputId = 1;
		}

		static public void waitingUserInputEnd() {
				waitingUserInputEnd(1);
			//	if (waitingUserInputId == 1) {
			///			System.out.println("LTManager.waitingUserInputEnd");
			//			if (waitingUserInputId == 0) {
			//					PvpContextUI.getActiveUI().notifyWarning("LTManager.waitingUserInputEnd waitingUserInput is 0");
			///			}
			//			waitingUserInputId = 0;
			//			if (tlr.get() != null) { // TODO should this be called on all LTRunners?
			///					tlr.get().interruptForUserInputEnd();
			//			}
			//	}
		}


		static public void waitingUserInputStart(int id) { // waitingUserInput
				System.out.println("LTManager.waitingUserInputStart: " + id + " > " + waitingUserInputId);
				if (id > waitingUserInputId) {

						//if (waitingUserInputId != 0) {
						//		PvpContextUI.getActiveUI().notifyWarning("LTManager.waitingUserInputStart waitingUserInput is : " + waitingUserInputId);
						//}
						waitingUserInputId = id;
				}
		}

		static public void waitingUserInputEnd(int id) {
				System.out.println("LTManager.waitingUserInputEnd: " + id + " == " + waitingUserInputId);
				if (id == waitingUserInputId) {
						//if (waitingUserInputId == 1) {
					//			System.out.println("LTManager.waitingUserInputEnd");
						//		if (waitingUserInputId == 0) {
						//				PvpContextUI.getActiveUI().notifyWarning("LTManager.waitingUserInputEnd waitingUserInput is 0");
						//		}
								waitingUserInputId = 0;
								if (tlr.get() != null) { // TODO should this be called on all LTRunners?
										tlr.get().interruptForUserInputEnd();
								}
						}
			//	}
		}

		static public boolean isWaitingUserInput() {
				return waitingUserInputId != 0;
		}

}
