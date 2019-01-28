/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

public class LTManager {

		private static LTManager lockObject = new LTManager(); // this is only used for synchronized

		private static ThreadLocal<LTRunner> tlr = new ThreadLocal<>();

		/**
		 * This is true if some user input UI is showing now. So that means don't make the cancel dialog visible - because we are waiting for user input.
		 */
		private static boolean waitingUserInput = false;

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
				Thread ltThread = new Thread(r, "LTManager Thread");
				ltThread.start();
		}

		static private LTRunnerSync createLTRunner(LongTask lt, String desc) {
				LTRunnerSync r = new LTRunnerSync(lt, desc);
				return r;
		}

		static void registerSyncThread(LTRunner r) {
				synchronized (lockObject) { // TODO is the correct?
						if (tlr.get() != null) {
								throw new RuntimeException("cannot start new LongTask when one is running on the current thread");
						}
						tlr.set(r);
				}
		}

		static void clearSyncThread() {
				synchronized (lockObject) { // TODO is the correct?
						tlr.remove();
				}
		}

		/**
		 * Run a LongTask synchronous.
		 */
		static public void runSync(LongTask lt, String desc) throws Exception {
				LTRunnerSync r = createLTRunner(lt, desc);
				r.runLongTask();
		}

		/**
		 * Run a LongTask synchronous.
		 */
		static public void runSync(LongTaskNoException lt, String desc) {
				LTRunnerSync r = createLTRunner(lt, desc);
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
				LTRunnerSync r = createLTRunner(lt, desc);
				return r.runLongTask();
		}

		/**
		 * Run a LongTask synchronous.
		 * Returns true if it was canceled.
		 */
		static public boolean runSync(CancelableLongTaskNoEception lt, String desc) {
				LTRunnerSync r = createLTRunner(lt, desc);
				boolean ret = true;
				try {
						ret = r.runLongTask();
				} catch (Exception e) {
						// There should not be an exception
						PvpContextUI.getActiveUI().notifyWarning("runSync had unexpected exception", e);
				}
				return ret;
		}

		static public void nextStep(String stepDesc) {
				LTRunner r = tlr.get();
				if (r == null) {
						PvpContextUI.getActiveUI().notifyWarning("Called nextStep when no LongTask active: " + stepDesc, new Exception());
				} else {
						r.nextStep(stepDesc);
				}
		}

		static public void stepDone(String stepDesc) {
				LTRunner r = tlr.get();
				if (r == null) {
						PvpContextUI.getActiveUI().notifyWarning("Called stepDone when no LongTask active: " + stepDesc, new Exception());
				} else {
						r.stepDone(stepDesc);
				}
		}

		static public void waitingUserInputStart() { // waitingUserInput
				synchronized (lockObject) {
						System.out.println("LTManager.waitingUserInputStart");
						waitingUserInput = true;
				}
		}

		static public void waitingUserInputEnd() {
				synchronized (lockObject) {
						System.out.println("LTManager.waitingUserInputEnd");
						waitingUserInput = false;

						if (tlr.get() != null) { // TODO should this be called on all LTRuuners?
								tlr.get().interruptForUserInputEnd();
						}
				}
		}

		static public boolean isWaitingUserInput() {
				return waitingUserInput;
		}

}
