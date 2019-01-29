/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import java.util.Objects;

/**
 * Handle the running of one LongTask.
 * For running asyncronously - will not show the Progress/Cancel dialog
 */
public class LTRunnerAsync extends LTRunner {

		private final LongTask lt;
		private final LTCallback cb;

		public LTRunnerAsync(LongTask ltParam) {
				this(ltParam, null);
		}

		public LTRunnerAsync(LongTask ltParam, LTCallback cbParam) {
				//if (ltParam == null) {
				//		throw new NullPointerException("LongTask cannot be null");
			//	}
				//lt = ltParam;
				lt = Objects.requireNonNull(ltParam, "LongTask must not be null");
				cb = cbParam == null ? new LTCallbackDefaultImpl() : cbParam;
		}

		@Override
		public void run() {
				LTManager.registerLTThread(this);
				cb.taskStarting(lt);
				try {
						lt.runLongTask();
				} catch (Exception e) {
						cb.handleException(lt, e);
				} finally {
						LTManager.clearLTThread();
						cb.taskComplete(lt);
				}
		}

		void nextStep(String stepDesc) {
			// TODO for now, do nothing with this. eventually this could be added to UI somehow
		}

		void stepDone(String stepDesc) {
		}
}
