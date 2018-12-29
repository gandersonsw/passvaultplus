/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

public interface LongTask {
		void runLongTask(LongTaskUI ui) throws Exception;

		void cancel(LongTaskUI ui);
}
