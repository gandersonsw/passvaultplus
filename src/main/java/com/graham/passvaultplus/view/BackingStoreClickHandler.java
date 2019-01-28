/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpBackingStore;
import com.graham.passvaultplus.model.core.PvpBackingStoreLTCB;
import com.graham.passvaultplus.view.longtask.LTManager;

public class BackingStoreClickHandler extends MouseAdapter {

	private final PvpContext context;
	private final PvpBackingStore backingStore;

	public BackingStoreClickHandler(final PvpContext contextParam, final PvpBackingStore bs) {
		context = contextParam;
		backingStore = bs;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (backingStore.getException() != null) {
			backingStore.userAskedToHandleError();
			context.ui.notifyInfo("BackingStoreClickHandler clicked : error");
		} else if (backingStore.isDirty()) {
			LTManager.run(context.data.getFileInterface().saveOneBackingStoreLT(context.data.getDataInterface(), backingStore), new PvpBackingStoreLTCB(backingStore));
		} else {
			context.ui.notifyInfo("BackingStoreClickHandler clicked : other");
		}
	}

}
