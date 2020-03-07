/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.util;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import javax.swing.JButton;
import javax.swing.JFrame;

public class SwingUtil {

	protected static Font bodyFont;

	public static void setFrameSizeAndCenter(java.awt.Component c, int width, int height) {
		com.graham.passvaultplus.PvpContextUI.checkEvtThread("0002");
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = dim.getWidth();//1024;
		double screenHeight = dim.getWidth();//768;

		c.setSize(width, height);
		c.setLocation((int)((screenWidth - width) / 2), (int)((screenHeight - height) / 3));
	}

	public static void center(java.awt.Component c) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = dim.getWidth();
		double screenHeight = dim.getHeight();
		Dimension d = c.getSize();

		if (d.getWidth() > screenWidth - 80 || d.getHeight() > screenHeight - 120) {
			d.setSize(d.getWidth() > screenWidth - 80 ? screenWidth - 80 : d.getWidth(), d.getHeight() > screenHeight - 120 ? screenHeight - 120 : d.getHeight());
			c.setSize(d);
		}
		com.graham.passvaultplus.PvpContextUI.checkEvtThread("0003");
		c.setLocation((int)((screenWidth - d.getWidth()) / 2), (int)((screenHeight - d.getHeight()) / 3));
	}

	public static void center(java.awt.Component c, JFrame owner) {
		com.graham.passvaultplus.PvpContextUI.checkEvtThread("0004");
		if (owner == null) {
			center(c);
		} else {
			Point loc = owner.getLocationOnScreen();
			Dimension osize = owner.getSize();
			Dimension size = c.getSize();
			c.setLocation(Math.max(loc.x + (int)((osize.width - size.width) / 2), 1), Math.max(loc.y + (int)((osize.height - size.height) / 3), 1));
		}
	}

	public static Font getBodyFont() {
		if (bodyFont == null) {
			bodyFont = new Font("Lucida Grande", Font.PLAIN, 10);
			if (bodyFont == null) {
				bodyFont = new Font("SansSerif", Font.PLAIN, 10);
			}
		}
		return bodyFont;
	}

	public static void makeButtonSmall(final JButton b) {
		b.putClientProperty("JComponent.sizeVariant", "small");
	}

}
