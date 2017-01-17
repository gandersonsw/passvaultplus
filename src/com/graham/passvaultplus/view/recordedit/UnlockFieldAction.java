/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JTextField;

public class UnlockFieldAction extends AbstractAction {
	final private JTextField tf;
	private String originalValue;
	final private RecordEditContext editContext;
	final private CopyFieldToClipboardAction copyAction;
	private boolean isLocked = true; // a field that is unlockable always starts as locked, for now

	public UnlockFieldAction(ImageIcon icon, JTextField tfParam, RecordEditContext editContextParam, CopyFieldToClipboardAction copyActionParam) {
		super(null, icon);
		tf = tfParam;
		originalValue = tfParam.getText();
		editContext = editContextParam;
		copyAction = copyActionParam;
		
		copyAction.setOverrideText(originalValue);
		
		tfParam.setText("******");
		tfParam.setEditable(false);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			editContext.setIgnoreAllChanges(true);
			// TODO - this needs to be an undoable action, as it will cause problems with the undo history
			if (isLocked) {
				tf.setText(originalValue);
				tf.setEditable(true);
				this.setEnabled(false); // TODO - for now, once they unlock, dont let it get locked again.  SaveEditorAction needs to be refactored to support this
				copyAction.setOverrideText(null);
			} else {
				originalValue = tf.getText();
				tf.setText("******");
				tf.setEditable(false);
				//this.setEnabled(false);
				copyAction.setOverrideText(originalValue);
			}
			isLocked = !isLocked;
		} finally {
			editContext.setIgnoreAllChanges(false);
		}
	}

}
