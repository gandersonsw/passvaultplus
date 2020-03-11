/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.framework;

public interface AppContext {

	/**
	 * @param message Informational message.
	 */
	void notifyInfo(String message);

	/**
	 * @param message Warning message.
	 * @param e Can be null
	 */
	void notifyWarning(String message, Exception e);

	/**
	 * @param e
	 * @param canContinue If true, the application can continue running. If false, the user will be notified,
	 *                     and the application will quit. This method will not return in that case.
	 */
	void notifyError(Exception e, boolean canContinue);

}
