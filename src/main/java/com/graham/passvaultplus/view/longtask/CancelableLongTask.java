/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

public interface CancelableLongTask extends LongTask {
		/**
		 * When this method returns, the task is considered canceled. It does not wait for the runLongTask to finish.
		 * @return True is the task was canceled. Return false if this task cannot be canceled at this point.
		 */
		boolean cancel();
}
