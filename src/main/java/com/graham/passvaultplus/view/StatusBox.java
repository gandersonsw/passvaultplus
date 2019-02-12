/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.*;

/**
 * Simple status feedback element with a fill color
 */
public class StatusBox extends JComponent {
	private static final long serialVersionUID = 1L;
	private static final Dimension SIZE = new Dimension(12,16);
	private static final Dimension MIN_SIZE = new Dimension(6,6);
	
	private volatile Color statusColor;
	private volatile SBAnimation sba;
	
	public StatusBox(Color c) {
		statusColor = c;
	}
	
	public void setColor(Color c) {
		synchronized(this) {
			if (sba != null) {
				sba.originalColor = c;
			}
			statusColor = c;
		}
		repaint();
	}

	synchronized void setColorOnly(Color c) {
		statusColor = c;
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

	synchronized public void startAnimation() {
		sba = new SBAnimation();
		sba.execute();
	}

	synchronized public void stopAnimation() {
		sba.cancel(true);
	}

	class SBAnimation extends SwingWorker<Void, Color> {
		int kf = 0;
		Color[] colors;
		volatile Color originalColor;
		public SBAnimation() {
			colors = new Color[10];
			originalColor = statusColor;
		}

		@Override
		protected Void doInBackground() throws Exception {
			Color next = originalColor;
			for (int i = 0; i < 5; i++) {
				colors[i] = next;
				colors[9 - i] = next;
				next = next.darker();
			}

			try {
				while (true) {
					publish(colors[kf]);
					kf = (kf + 1) % 10;
					Thread.sleep(100);
				}
			} catch (InterruptedException e) { }
			return null;
		}

		@Override
		protected void process(List<Color> chunks) {
			setColorOnly(chunks.get(0));
			repaint();
		}

		@Override
		protected void done() {
			setColorOnly(originalColor);
			repaint();
			sba = null;
		}
	}

}
