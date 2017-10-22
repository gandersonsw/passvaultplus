/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import com.graham.passvaultplus.model.core.PvpRecord;

public class RevertEditorAction extends AbstractAction {
	final private RecordEditContext editContext;

	public RevertEditorAction(final RecordEditContext editContextParam) {
		super("Revert");
		editContext = editContextParam;
	}

	public void actionPerformed(ActionEvent e) {
		final PvpRecord editedValues = new PvpRecord(editContext.editRecord.getType());
		try {
			editContext.setIgnoreAllChanges(true);
			editContext.populateRecordFromUI(editedValues);
			editContext.populateUIFromRecord();
			editContext.setHasUnsavedChanges(false);
		} finally {
			editContext.setIgnoreAllChanges(false);
		}
		
		editContext.undoManager.undoableEditHappened(new UndoableEditEvent(editedValues, new REUndoableEdit(editedValues)));
	}
	
	class REUndoableEdit implements UndoableEdit {
		
		final PvpRecord editedValues;
		
		public REUndoableEdit(PvpRecord editedValuesParam) {
			editedValues = editedValuesParam;
		}

		@Override
		public void undo() throws CannotUndoException {
			try {
				editContext.setIgnoreAllChanges(true);
				editContext.populateUIFromRecord(editedValues);
				editContext.setHasUnsavedChanges(true);
			} finally {
				editContext.setIgnoreAllChanges(false);
			}
		}

		@Override
		public boolean canUndo() {
			return true;
		}

		@Override
		public void redo() throws CannotRedoException {
			try {
				editContext.setIgnoreAllChanges(true);
				editContext.populateUIFromRecord();
				editContext.setHasUnsavedChanges(false);
			} finally {
				editContext.setIgnoreAllChanges(false);
			}
		}

		@Override
		public boolean canRedo() {
			return true;
		}

		@Override
		public void die() {
		}

		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			return false;
		}

		@Override
		public boolean replaceEdit(UndoableEdit anEdit) {
			return false;
		}

		@Override
		public boolean isSignificant() {
			return true;
		}

		@Override
		public String getPresentationName() {
			return "Revert";
		}

		@Override
		public String getUndoPresentationName() {
			return "Undo Revert";
		}

		@Override
		public String getRedoPresentationName() {
			return "Redo Revert";
		}
		
	}

}
