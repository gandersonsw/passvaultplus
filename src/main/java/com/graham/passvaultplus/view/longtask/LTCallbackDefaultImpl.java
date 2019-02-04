/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

public class LTCallbackDefaultImpl implements LTCallback {
		public void taskStarting(LTRunner lt) {
		}

		public void taskComplete(LTRunner lt) {
		}

		public void handleException(LTRunner lt, Exception e) {
				PvpContextUI.getActiveUI().notifyWarning("Exception in a LongTask", e);
		}
}
