/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.dashdoc.model;

public interface DdPart {

	/**
	 * @return True if adding was successful
	 */
	boolean addPart(DdPart p);

	void postLoadCleanup(DdContainer parent);

	/**
	 * @return True if this instance supports adding of given type.
	 */
	boolean supportsAdd(DdPart part);
}
