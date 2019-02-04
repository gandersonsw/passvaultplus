/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import com.graham.passvaultplus.PvpContextUI;

import java.util.ArrayList;
import java.util.List;

/**
 * Handle the running of one LongTask.
 */
public abstract class LTRunner implements Runnable {

		private List<Runnable> cancelCallbacks = null;
		volatile boolean wasCanceled;

		abstract void nextStep(String stepDesc);

		abstract void stepDone(String stepDesc);

		void interruptForUserInputEnd() {
				// do nothing by default
		}

		void registerCancelCB(Runnable r) {
				if (cancelCallbacks == null) {
						cancelCallbacks = new ArrayList<>();
				}
				cancelCallbacks.add(r);
		}

		void unregisterCancelCB(Runnable r) {
				if (cancelCallbacks == null) {
						PvpContextUI.getActiveUI().notifyWarning("LTRunner.unregisterCancelCB :: cancelCallbacks is null");
						return;
				}
				if (cancelCallbacks.size() < 1) {
						PvpContextUI.getActiveUI().notifyWarning("LTRunner.unregisterCancelCB :: cancelCallbacks is empty");
						return;
				}
				if (r == cancelCallbacks.get(cancelCallbacks.size() - 1)) {
						cancelCallbacks.remove(cancelCallbacks.size() - 1);
				} else {
						PvpContextUI.getActiveUI().notifyWarning("LTRunner.unregisterCancelCB :: object not equal");
				}
		}

		/**
		 * Returns trye i
		 */
		public void cancel() {
				if (wasCanceled) {
						PvpContextUI.getActiveUI().notifyWarning("LTRunner.unregisterCancelCB.A :: wasCanceled == true");
				}
				if (cancelCallbacks == null) {
						PvpContextUI.getActiveUI().notifyWarning("LTRunner.unregisterCancelCB.B :: cancelCallbacks == null");
						return;
				}
				for (Runnable r : cancelCallbacks) {
						r.run();
						wasCanceled = true;
				}
				if (wasCanceled) {
						killStuff();
				} else {
						PvpContextUI.getActiveUI().notifyWarning("LTRunner.unregisterCancelCB.C :: wasCanceled == false");
						//	runner.setCannotCancel();
				}
		}

		void killStuff() {
				// by default, do nothing
		}

		boolean canCancelNow() {
				return cancelCallbacks != null && cancelCallbacks.size() > 0;
		}
}
