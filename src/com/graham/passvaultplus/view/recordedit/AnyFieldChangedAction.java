/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.graham.passvaultplus.model.core.PvpRecord;

public class AnyFieldChangedAction extends AbstractAction {
	final private RecordEditField ref;
	final private RecordEditContext editContext;

	public AnyFieldChangedAction(final RecordEditContext editContextParam, final RecordEditField refParam) {
		editContext = editContextParam;
		ref = refParam;
	}
	public void actionPerformed(ActionEvent e) {
		if (editContext.shouldIngoreChanges()) {
			// System.out.println("ignoring change event");
			return;
		}

		boolean hasOriginalValue = ref.isEdited(editContext.editRecord);
		
		boolean recordHasBeenModified = true;
		// the currently edited field has its original value, so check to see if all the other fields are the same
		if (hasOriginalValue) {
			PvpRecord tempRec = new PvpRecord(editContext.editRecord.getType());
			editContext.populateRecordFromUI(tempRec);
			if (tempRec.isSimilar(editContext.editRecord)) {
				recordHasBeenModified = false;
			}
		}

		editContext.setHasUnsavedChanges(recordHasBeenModified);
	}
}