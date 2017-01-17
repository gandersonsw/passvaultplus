/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class ButtonHiliteMouseListener implements MouseListener {

	// TODO this doesnt work - removes old button border

	static final EmptyBorder normalBorder = new EmptyBorder(3,3,3,3);
	static final MatteBorder hiliteBorder = new MatteBorder(3,3,3,3, Color.CYAN);

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (e.getSource() != null && e.getSource() instanceof JButton) {
			final JButton b = (JButton)e.getSource();
			b.setBorder(hiliteBorder);
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (e.getSource() != null && e.getSource() instanceof JButton) {
			final JButton b = (JButton)e.getSource();
			b.setBorder(normalBorder);
		}
	}
}
