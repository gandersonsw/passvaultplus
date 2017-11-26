/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

/**
 * Simple status feedback element with a fill color
 */
public class StatusBox extends JComponent {
	private static final long serialVersionUID = 1L;
	private static final Dimension SIZE = new Dimension(12,16);
	private static final Dimension MIN_SIZE = new Dimension(6,6);
	
	Color statusColor;
	
	public StatusBox(Color c) {
		statusColor = c;
	}
	
	public void setColor(Color c) {
		statusColor = c;
		this.repaint();
		// TODO repaint
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Dimension size = this.getSize();
        g.setColor(statusColor);
        g.fillRect(0, 0, size.width, size.height - 4);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, size.width - 1, size.height - 5);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return SIZE;
	}
	
	@Override
	public Dimension getMaximumSize() {
		return SIZE;
	}
	
	@Override
	public Dimension getMinimumSize() {
		return MIN_SIZE;
	}

}
