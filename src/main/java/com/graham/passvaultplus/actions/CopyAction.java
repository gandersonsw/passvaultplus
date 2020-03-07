/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import com.graham.util.ResourceUtil;

public class CopyAction extends AbstractAction {

	public CopyAction() {
		super(null, ResourceUtil.getIcon("copy"));
	}

	public void actionPerformed(ActionEvent e) {

		KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		Component c = kfm.getFocusOwner();

		if (c instanceof JComponent) {
			JComponent jc = (JComponent)c;

			Action copy = jc.getActionMap().get("copy");
			if (copy != null) {
				ActionEvent ae = new ActionEvent(jc, ActionEvent.ACTION_PERFORMED, "");
				copy.actionPerformed(ae);
			}
		}

	}
}
