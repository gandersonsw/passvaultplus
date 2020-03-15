/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

import java.awt.*;

public class LTManager {

		/**
		 * This is non-zero if some user input UI is showing now. So that means don't make the cancel dialog visible - because we are waiting for user input.
		 */
		private static volatile int waitingUserInputCount = 0;

	//	static public boolean isLTThread() {
	//		return Thread.currentThread().getName().equals(LTThread.THREAD_NAME);
	//	}

		static private LTRunner getLTThreadRunner() {
				Thread t = Thread.currentThread();
				if (t instanceof LTThread) {
						LTThread ltt = (LTThread) t;
						return ltt.rnr;
				}
				return null;
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
								lt.runLongTask(ltr);
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
								lt.runLongTask(ltr);
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

		static public Window waitingUserInputStart() {
				Window cancelW = null;
				if (waitingUserInputCount == 0) {
						//waitingUserInputId = id;
						for (Window w : Frame.getWindows()) {
								if (w instanceof LongTaskUI) {
										if (cancelW != null) {
												PvpContextUI.getActiveUI().notifyWarning("LTManager.waitingUserInputStart :: more than one LongTaskUI. This in not handled correctly");
										}
										cancelW = w;
								}
						}
				}
				waitingUserInputCount++;
				return cancelW;
		}

		static public void waitingUserInputEnd() {
				if (waitingUserInputCount == 0) {
						PvpContextUI.getActiveUI().notifyWarning("LTManager.waitingUserInputStart :: waitingUserInputCount is already 0. This should not happen. waitingUserInputEnd was called too many times?", new Exception());
				}
				waitingUserInputCount--;
				if (waitingUserInputCount == 0) {
						LTRunner ltr = getLTThreadRunner();
						if (ltr != null) { // TODO should this be called on all LTRunners?
							ltr.interruptForUserInputEnd();
						}
				}
		}

		static public boolean isWaitingUserInput() {
				return waitingUserInputCount != 0;
		}

}
