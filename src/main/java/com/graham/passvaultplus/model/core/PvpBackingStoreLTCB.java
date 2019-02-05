/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.view.longtask.LTCallback;
import com.graham.passvaultplus.view.longtask.LTRunner;

/**
 * PassVaultPlus BackingStore LongTask CallBack - pvpBsLtCb
 */
public class PvpBackingStoreLTCB implements LTCallback {

		final private PvpBackingStore bs;

		public PvpBackingStoreLTCB(PvpBackingStore bsParam) {
				bs = bsParam;
		}

		@Override
		public void taskStarting(LTRunner lt) {
				bs.getStatusBox().startAnimation();
		}

		@Override
		public void taskComplete(LTRunner lt) {
				bs.getStatusBox().stopAnimation();
		}

		@Override
		public void handleException(LTRunner lt, Exception e) {
				bs.setException(new PvpException(PvpException.GeneralErrCode.CantOpenDataFile, e));
				// TODO bs.setException();
		}
}
