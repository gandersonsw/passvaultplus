/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

/**
 * Handle the running of one LongTask.
 * For running asyncronously - will not show the Progress/Cancel dialog
 */
public class LTRunnerAsync extends LTRunner {

		private final LongTask lt;
		private final LTCallback cb;

		public LTRunnerAsync(LongTask ltParam, LTCallback cbParam) {
				lt = ltParam;
				cb = cbParam;
		}

		@Override
		public void run() {
				LTManager.registerSyncThread(this);
				cb.taskStarting(lt);
				try {
						lt.runLongTask();
				} catch (Exception e) {
						if (cb == null) {
								PvpContextUI.getActiveUI().notifyWarning("Exception in LTRunnerAsync", e);
						} else {
								cb.handleException(lt, e);
						}
				} finally {
						LTManager.clearSyncThread();
						if (cb != null) {
								cb.taskComplete(lt);
						}
				}
		}

		void nextStep(String stepDesc) {
			// TODO for now, do nothing with this. eventually this could be added to UI somehow
		}

		void stepDone(String stepDesc) {
		}
}
