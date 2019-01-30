/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.view.longtask.LTCallback;
import com.graham.passvaultplus.view.longtask.LTRunnerAsync;
import com.graham.passvaultplus.view.longtask.LongTask;

public class PvpBackingStoreLTCB implements LTCallback {

		final private PvpBackingStore bs;

		public PvpBackingStoreLTCB(PvpBackingStore bsParam) {
				bs = bsParam;
		}

		@Override
		public void taskStarting(LTRunnerAsync lt) {
				bs.getStatusBox().startAnimation();
		}

		@Override
		public void taskComplete(LTRunnerAsync lt) {
				bs.getStatusBox().stopAnimation();
		}

		@Override
		public void handleException(LTRunnerAsync lt, Exception e) {
				bs.setException(new PvpException(PvpException.GeneralErrCode.CantOpenDataFile, e));
				// TODO bs.setException();
		}
}
