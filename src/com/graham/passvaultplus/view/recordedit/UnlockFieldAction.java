/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public class UnlockFieldAction extends AbstractAction {
	final private RecordEditContext editContext;
	private boolean isLocked = true; // a field that is unlockable always starts as locked, for now
	final RecordEditFieldSecret refs;

	public UnlockFieldAction(ImageIcon icon, RecordEditFieldSecret refsParam, RecordEditContext editContextParam) {
		super(null, icon);
		refs = refsParam;
		editContext = editContextParam;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		flipIsLocked();
		editContext.undoManager.undoableEditHappened(new UndoableEditEvent(this, new UFUndoableEdit()));
	}
	
	private void flipIsLocked() {
		try {
			editContext.setIgnoreAllChanges(true);
			isLocked = !isLocked;
			refs.setIsLockedAndHidden(isLocked);
		} finally {
			editContext.setIgnoreAllChanges(false);
		}
	}


	class UFUndoableEdit implements UndoableEdit {
		
		@Override
		public void undo() throws CannotUndoException {
			flipIsLocked();
		}

		@Override
		public boolean canUndo() {
			return true;
		}

		@Override
		public void redo() throws CannotRedoException {
			flipIsLocked();
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
			return "Unlock";
		}

		@Override
		public String getUndoPresentationName() {
			return "Undo Unlock";
		}

		@Override
		public String getRedoPresentationName() {
			return "Redo Unlock";
		}
		
	}

}
