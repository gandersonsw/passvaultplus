/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.swingui.popupwidgets;

import java.awt.Component;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JFrame;
import javax.swing.JWindow;

public abstract class AbstractPopupWidget<C extends Component> implements ComponentListener {

	final JFrame parentFrame;
	final JWindow popupWindow;
	final C parentComponent;
	
	public AbstractPopupWidget(final JFrame o, final C c) {
		parentFrame = o;
		parentComponent = c;
		popupWindow = new JWindow(parentFrame);
		popupWindow.setType(Window.Type.POPUP);
		popupWindow.setAutoRequestFocus(false);
		popupWindow.setFocusableWindowState(false);
		popupWindow.setFocusable(false);
		o.addComponentListener(this);
	}
	
	void updateLocationRelativeToParent() {
		final Point p = parentComponent.getLocationOnScreen();
		p.translate(0, (int)parentComponent.getSize().getHeight());
		popupWindow.setLocation(p);
	}
	
	public void close() {
		parentFrame.removeComponentListener(this);
		popupWindow.setVisible(false);
	}
	
	@Override
	public void componentResized(ComponentEvent e) {
		updateLocationRelativeToParent();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		updateLocationRelativeToParent();
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}
	
	public void handleEnter() {
		// by default, do nothing
	}

}
