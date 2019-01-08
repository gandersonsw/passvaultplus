/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpContextUI;

import javax.swing.*;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.net.URI;

public class GoToUrlAction extends AbstractAction {
	final private String link;

	public static void checkAndAdd(final JComponent c, final String linkParam) {
		if (Desktop.isDesktopSupported()) {
			final JButton gotoButton = new JButton(new GoToUrlAction(linkParam));
			BCUtil.makeButtonSmall(gotoButton);
			gotoButton.setFocusable(false);
			c.add(gotoButton);
		}
	}

	public GoToUrlAction(final String linkParam) {
		super("Go to Link");
		link = linkParam;
	}

	public void actionPerformed(ActionEvent evt) {
		if (Desktop.isDesktopSupported()) {
			URI uri;
			try {
				uri = new URI(link);
				Desktop.getDesktop().browse(uri);
			} catch (Exception ex) {
					// TODO test this line when main ui not showing
				PvpContextUI.getActiveUI().showMessageDialog("Error", "There was an error opening link: " + ex.getMessage());
			}
		}
	}
}
