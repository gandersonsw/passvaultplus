/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

public interface LTCallback {
		void taskStarting(LTRunner lt);

		void taskComplete(LTRunner lt);

		void handleException(LTRunner lt, Exception e);
}
