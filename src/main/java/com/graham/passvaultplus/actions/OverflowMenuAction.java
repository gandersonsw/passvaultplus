/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import com.graham.swingui.OverflowLayout;
import com.graham.util.ResourceUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class OverflowMenuAction extends AbstractAction {

		final private OverflowLayout ofLayout;

		public OverflowMenuAction(OverflowLayout ofLayoutParam) {
				super(null, ResourceUtil.getIcon("down-arrow"));
				ofLayout = ofLayoutParam;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof Component) {
						final JPopupMenu popup = new JPopupMenu();
						for (Component c : ofLayout.getOverflowHiddenComponents()) {
								if (c instanceof JButton) {
										JMenuItem mi = new JMenuItem(((JButton) c).getAction());
										popup.add(mi);
								}
						}
						popup.setFocusable(false);
						Component c = (Component)e.getSource();
						popup.show(c, 0, c.getHeight());
				}
		}
}
