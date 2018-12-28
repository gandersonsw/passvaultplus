/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import com.graham.passvaultplus.PvpContext;

import java.awt.Component;

public interface OtherTabBuilder {

	String getTitle();

	Component build(PvpContext context);

	void dispose();
}
