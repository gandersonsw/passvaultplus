/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import java.util.Objects;

/**
 * Handle the running of one LongTask.
 * For running asyncronously - will not show the Progress/Cancel dialog.
 * Meaning UI is chugging along, but this is running in the background.
 * We are not blocking the user from using the app.
 */
public class LTRunnerAsync extends LTRunner {

		private final LongTask lt;
		private final LTCallback cb;

		public LTRunnerAsync(LongTask ltParam, LTCallback cbParam) {
				lt = Objects.requireNonNull(ltParam, "LongTask must not be null");
				cb = cbParam == null ? new LTCallbackDefaultImpl() : cbParam;
		}

		@Override
		public void run() {
				LTManager.registerLTThread(this);
				cb.taskStarting(this);
				try {
						lt.runLongTask();
				} catch (Exception e) {
						cb.handleException(this, e);
				} finally {
						LTManager.clearLTThread();
						cb.taskComplete(this);
				}
		}

		void nextStep(String stepDesc) throws LTCanceledException {
				if (wasCanceled) {
						throw new LTCanceledException();
				}
			// TODO for now, do nothing with this. eventually this could be added to UI somehow
		}

		void stepDone(String stepDesc) throws LTCanceledException {
				if (wasCanceled) {
						throw new LTCanceledException();
				}
		}
}
