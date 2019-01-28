/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

/**
 * Handle the running of one LongTask.
 */
public abstract class LTRunner implements Runnable {
		abstract void nextStep(String stepDesc);

		abstract void stepDone(String stepDesc);

		void interruptForUserInputEnd() {
				// do nothing by default
		}
}
