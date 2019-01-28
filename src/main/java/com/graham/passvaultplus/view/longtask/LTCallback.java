/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

public interface LTCallback {
		void taskStarting(LongTask lt);

		void taskComplete(LongTask lt);

		void handleException(LongTask lt, Exception e);
}
