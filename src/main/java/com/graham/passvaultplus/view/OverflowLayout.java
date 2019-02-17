/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.*;
import java.util.ArrayList;

/**
 * Similar to FlowLayout. But has an item on the right side that shows if everything does not fit.
 */
public class OverflowLayout implements LayoutManager, java.io.Serializable {
		private FlowLayout fLayout = new FlowLayout(FlowLayout.LEADING);

		private java.util.List<Component> overflowHiddenList = new ArrayList<>();

		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
				return fLayout.preferredLayoutSize(parent);
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
				return fLayout.minimumLayoutSize(parent);
		}

		@Override
		public void layoutContainer(Container parent) {
				synchronized (parent.getTreeLock()) {
						Insets insets = parent.getInsets();
						int maxwidth = parent.getWidth() - (insets.left + insets.right + fLayout.getHgap() * 2);
						int nmembers = parent.getComponentCount();
						int x = insets.left + fLayout.getHgap(), y = insets.top;
						boolean addedOverflowButton = false;

						Component overflowButton = parent.getComponent(nmembers - 1);
						int ofWidth = overflowButton.getPreferredSize().width;

						overflowHiddenList = new ArrayList<>();

						for (int i = 0; i < nmembers - 1; i++) {
								Component m = parent.getComponent(i);
								if (m.isVisible()) {
										Dimension d = m.getPreferredSize();
										m.setSize(d.width, d.height);

										int cy = y + (parent.getHeight() - m.getHeight()) / 2;

										if (x + d.width + fLayout.getHgap() + ofWidth > maxwidth && i < nmembers - 2) {
												Dimension ofd = overflowButton.getPreferredSize();
												overflowButton.setSize(ofd.width, ofd.height);
												cy = y + (parent.getHeight() - overflowButton.getHeight()) / 2;
												overflowButton.setLocation(parent.getWidth() - ofd.width - insets.right - fLayout.getHgap(), cy);
												addedOverflowButton = true;
										}

										if (addedOverflowButton) {
												m.setLocation(x, y + parent.getHeight() * 2); //hide it
												overflowHiddenList.add(m);
										} else {
												if (i == nmembers - 2) {
														m.setLocation(parent.getWidth() - d.width - insets.right - fLayout.getHgap(), cy);
												} else {
														m.setLocation(x, cy);
												}

												if (x > 0) {
														x += fLayout.getHgap();
												}
												x += d.width;
										}
								}
						}

						if (!addedOverflowButton) {
								overflowButton.setLocation(x, y + parent.getHeight() * 2); // hide it
						}
				}
		}

		public java.util.List<Component> getOverflowHiddenComponents() {
				return overflowHiddenList;
		}

}
