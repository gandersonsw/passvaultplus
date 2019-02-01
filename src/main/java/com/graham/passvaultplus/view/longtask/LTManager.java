/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

public class LTManager {

		private static ThreadLocal<LTRunner> tlr = new ThreadLocal<>();

		/**
		 * This is true if some user input UI is showing now. So that means don't make the cancel dialog visible - because we are waiting for user input.
		 */
		private static volatile boolean waitingUserInput = false;

		/**
		 * Run a LongTask asynchronous.
		 */
		static public void run(LongTask lt) {
				run(lt, null);
		}

		/**
		 * Run a LongTask asynchronous.
		 */
		static public void run(LongTask lt, LTCallback cb) {
				LTRunnerAsync r = new LTRunnerAsync(lt, cb);
				Thread ltThread = new Thread(r, "ltay"); // LongTask Async thread
				ltThread.start();
		}

		static void registerLTThread(LTRunner r) {
				if (tlr.get() != null) {
						throw new RuntimeException("cannot start new LongTask when one is running on the current thread");
				}
				tlr.set(r);
		}

		static void clearLTThread() {
				tlr.remove();
		}

		/**
		 * Run a LongTask synchronous.
		 */
		static public void runSync(LongTask lt, String desc) throws Exception {
				LTRunnerSync r = new LTRunnerSync(lt, desc);
				r.runLongTask();
		}

		/**
		 * Run a LongTask synchronous.
		 */
		static public void runSync(LongTaskNoException lt, String desc) {
				LTRunnerSync r = new LTRunnerSync(lt, desc);
				try {
						r.runLongTask();
				} catch (Exception e) {
						// There should not be an exception
						PvpContextUI.getActiveUI().notifyWarning("runSync had unexpected exception", e);
				}
		}

		/**
		 * Run a LongTask synchronous.
		 * Returns true if it was canceled.
		 */
		static public boolean runSync(CancelableLongTask lt, String desc) throws Exception {
				LTRunnerSync r = new LTRunnerSync(lt, desc);
				return r.runLongTask();
		}

		/**
		 * Run a LongTask synchronous.
		 * Returns true if it was canceled.
		 */
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
				System.out.println("LTManager.waitingUserInputStart");
				if (waitingUserInput == true) {
						PvpContextUI.getActiveUI().notifyWarning("LTManager.waitingUserInputStart waitingUserInput is true");
				}
				waitingUserInput = true;
		}

		static public void waitingUserInputEnd() {
				System.out.println("LTManager.waitingUserInputEnd");
				if (waitingUserInput == false) {
						PvpContextUI.getActiveUI().notifyWarning("LTManager.waitingUserInputEnd waitingUserInput is false");
				}
				waitingUserInput = false;
				if (tlr.get() != null) { // TODO should this be called on all LTRunners?
						tlr.get().interruptForUserInputEnd();
				}
		}

		static public boolean isWaitingUserInput() {
				return waitingUserInput;
		}

}
