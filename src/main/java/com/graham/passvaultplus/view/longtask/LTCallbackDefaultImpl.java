/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

public class LTCallbackDefaultImpl implements LTCallback {
		public void taskStarting(LTRunnerAsync lt) {
		}

		public void taskComplete(LTRunnerAsync lt) {
		}

		public void handleException(LTRunnerAsync lt, Exception e) {
				PvpContextUI.getActiveUI().notifyWarning("Exception in LTRunnerAsync", e);
		}
}
