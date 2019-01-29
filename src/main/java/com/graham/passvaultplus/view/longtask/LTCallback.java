/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

public interface LTCallback {
		void taskStarting(LTRunnerAsync lt);

		void taskComplete(LTRunnerAsync lt);

		void handleException(LTRunnerAsync lt, Exception e);
}
