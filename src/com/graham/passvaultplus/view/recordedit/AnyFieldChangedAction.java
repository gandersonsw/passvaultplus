/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextField;

import com.graham.passvaultplus.AppUtil;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;

public class AnyFieldChangedAction extends AbstractAction {
	final private String fieldName;
	final private RecordEditContext editContext;

	public AnyFieldChangedAction(final RecordEditContext editContextParam, String fieldNameParam) {
		editContext = editContextParam;
		fieldName = fieldNameParam;
	}
	public void actionPerformed(ActionEvent e) {
		if (editContext.ignoreChangeEvents) {
			System.out.println("ignoring change event");
			return;
		}

		boolean hasOriginalValue = false;
		// TODO clean up this if test everywhere
		if (fieldName.equals(PvpField.USR_CATEGORY)) {

			PvpRecord cat = editContext.getSelectedCategory();
			if (AppUtil.equalsWithNull(cat, editContext.editRecord.getCategory())) {
				hasOriginalValue = true;
			}

		} else {
			String val = editContext.editRecord.getCustomField(fieldName);
			if (val == null) {
				val = "";
			}
			JTextField tf = (JTextField)editContext.editFields.get(fieldName);
			if (val.equals(tf.getText())) {
				hasOriginalValue = true;
			}
		}

		boolean recordHasBeenModified = true;
		// the currently edited field has its original value, so check to see if all the other fields are the same
		if (hasOriginalValue) {
			//System.out.println("777 at 1");
			PvpRecord tempRec = new PvpRecord(editContext.editRecord.getType());
			editContext.populateRecordFromUI(tempRec);
			if (tempRec.isSimilar(editContext.editRecord)) {
				recordHasBeenModified = false;
				//System.out.println("777 at 2");
			}
		}

		editContext.setHasUnsavedChanges(recordHasBeenModified);
		//editContext.revertAction.setEnabled(recordHasBeenModified);
		//editContext.saveAction.setEnabled(recordHasBeenModified);
	}
}