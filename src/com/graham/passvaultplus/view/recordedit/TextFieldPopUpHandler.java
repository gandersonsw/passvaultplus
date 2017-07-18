/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentListener;

import com.graham.passvaultplus.actions.TextFieldChangeForwarder;
import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpType;
import com.graham.swingui.popupwidgets.AbstractPopupWidget;
import com.graham.swingui.popupwidgets.DatePicker;
import com.graham.swingui.popupwidgets.TypeAheadSelector;

public class TextFieldPopUpHandler implements FocusListener {
	
	/** there can only ever be one pop up showing at a time */
	private static TextFieldPopUpHandler lastFocused = null;

	private final PvpField field;
	private final JFrame mainFrame;
	private final JTextField textField;
	private final PvpDataInterface dataInterface;
	private final PvpType recordType;
	private final TextFieldChangeForwarder documentListener;
	private boolean pressedEscape = false;
	private boolean pressedEnter = false;
	
	private AbstractPopupWidget popup;
	
	public TextFieldPopUpHandler(final JFrame mf, final JTextField tf, final PvpField f, final PvpType recordTypeParam, PvpDataInterface di) {
		field = f;
		mainFrame = mf;
		textField = tf;
		dataInterface = di;
		recordType = recordTypeParam;
		documentListener = new TextFieldChangeForwarder(new DocumentAnyChangeListener());
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (lastFocused != this) {
			if (lastFocused != null) {
				lastFocused.focusLost(null);
			}
			pressedEscape = false;
			pressedEnter = false;
			lastFocused = this;
		}
		showPopUp();
	}
	
	private void showPopUp() {
		if (pressedEscape) {
			return;
		}
			
 		if (popup != null) {
			return;
		}
		
		if (field == null || field.getType() == null) {
			return;
		}
		
		if (field.isClassificationSecret()) {
			return;
		}
		
		if (field.getType().equals(PvpField.TYPE_DATE)) {
			popup = new DatePicker(mainFrame, textField, false);
			return;
		}
		
		List<String> values = dataInterface.getCommonFiledValues(recordType.getName(), field.getName());
		if (TypeAheadSelector.shouldShow(textField, values, pressedEnter)) {
			popup = new TypeAheadSelector(mainFrame, textField, values);
			return;
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		hidePopUp();
	}
	
	private void hidePopUp() {
		if (popup != null) {
			popup.close();
			popup = null;
		}
	}
	
	public DocumentListener getDocumentListener() {
		return documentListener;
	}
	
	public void addInputAndActionMapItems() {
		textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escapepuw");
		textField.getActionMap().put("escapepuw", new EscapeHandler());
		
		textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "returnpuw");
		textField.getActionMap().put("returnpuw", new EnterHandler());
	}
	
	class DocumentAnyChangeListener extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (lastFocused == TextFieldPopUpHandler.this) {
				showPopUp();
			}
		}
	}
	
	class EscapeHandler extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			pressedEscape = true;
			pressedEnter = false;
			hidePopUp();
		}
	}
	
	class EnterHandler extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (popup != null) {
				popup.handleEnter();
			}
			
			pressedEscape = false;
			pressedEnter = true;
			focusGained(null);
		}
	}
}
