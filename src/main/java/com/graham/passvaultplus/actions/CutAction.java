/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import com.graham.passvaultplus.PvpContext;

public class CutAction extends AbstractAction {

	public CutAction() {
		super(null, PvpContext.getIcon("cut"));
	}

	public void actionPerformed(ActionEvent e) {

		KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		Component c = kfm.getFocusOwner();

		if (c instanceof JComponent) {
			JComponent jc = (JComponent)c;

			Action a = jc.getActionMap().get("cut");
			if (a != null) {
				ActionEvent ae = new ActionEvent(jc, ActionEvent.ACTION_PERFORMED, "");
				a.actionPerformed(ae);
			}
		}

	}
}
