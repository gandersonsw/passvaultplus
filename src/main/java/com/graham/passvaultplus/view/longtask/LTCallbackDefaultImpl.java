package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

public class LTCallbackDefaultImpl implements LTCallback {
		public void taskStarting(LongTask lt) {
		}

		public void taskComplete(LongTask lt) {
		}

		public void handleException(LongTask lt, Exception e) {
				PvpContextUI.getActiveUI().notifyWarning("Exception in LTRunnerAsync", e);
		}
}
