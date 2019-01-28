/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import com.graham.passvaultplus.view.longtask.LTCallback;
import com.graham.passvaultplus.view.longtask.LongTask;

public class PvpBackingStoreLTCB implements LTCallback {

		final private PvpBackingStore bs;

		public PvpBackingStoreLTCB(PvpBackingStore bsParam) {
				bs = bsParam;
		}

		@Override
		public void taskStarting(LongTask lt) {
				bs.getStatusBox().startAnimation();
		}

		@Override
		public void taskComplete(LongTask lt) {
				bs.getStatusBox().stopAnimation();
		}

		@Override
		public void handleException(LongTask lt, Exception e) {
				// TODO bs.setException();
		}
}
