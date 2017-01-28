/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class UnlockFieldAction extends AbstractAction {
	final private RecordEditContext editContext;
	private boolean isLocked = true; // a field that is unlockable always starts as locked, for now
	final RecordEditFieldSecret refs;

	public UnlockFieldAction(ImageIcon icon, RecordEditFieldSecret refsParam, RecordEditContext editContextParam) {
		super(null, icon);
		refs = refsParam;
		editContext = editContextParam;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			editContext.setIgnoreAllChanges(true);
			isLocked = !isLocked;
			refs.setIsLockedAndHidden(isLocked);
		} finally {
			editContext.setIgnoreAllChanges(false);
		}
	}

}
