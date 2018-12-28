/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;

public class DeleteRecord extends AbstractAction {
	final private PvpContext context;

	public DeleteRecord(final PvpContext contextParam) {
		super(null, PvpContext.getIcon("delete"));
		context = contextParam;
	}

	public void actionPerformed(ActionEvent e) {
		Collection<PvpRecord> records = null;
		ArrayList<RecordEditContext> editContexts = new ArrayList<>();
		String message = null;
		if (context.uiMain.isCurrentTabList()) {
			records = context.uiMain.getViewListContext().getAllSelectedRecords();

			for (PvpRecord r : records) {
				final RecordEditContext re2 = context.uiMain.getRecordEditor(r);
				if (re2 != null) {
					editContexts.add(re2);
				}
			}

			if (records.size() == 1) {
				message = "Are you sure you want to delete the record?";
			} else if (records.size() > 1) {
				message = "Are you sure you want to delete " + records.size() + " records?";
			}

		} else {
			RecordEditContext ec2 = context.uiMain.getCurrentTabRecordEditContext();

			if (ec2 != null) {
				editContexts.add(ec2);
				ArrayList<PvpRecord> r2 = new ArrayList<>();
				r2.add(ec2.getRecord());

				if (ec2.getRecord().isPersisted()) {
					message = "Are you sure you want to delete the record?";
				}
				records = r2;
			}
		}

		if (message != null) {
			boolean b = context.ui.showConfirmDialog("Delete", message); //  JOptionPane.showConfirmDialog(context.ui.getMainFrame(), message, "Delete", JOptionPane.OK_CANCEL_OPTION);
			if (!b) {
				return;
			}
		}

		for (RecordEditContext ec2 : editContexts) {
			context.uiMain.removeRecordEditor(ec2);
		}
		if (records != null) {
			context.data.getDataInterface().deleteRecords(records);
			context.uiMain.getUndoManager().undoableEditHappened(new UndoableEditEvent(records, new DRUndoableEdit(records)));
		}

	}


	class DRUndoableEdit implements UndoableEdit {

		final Collection<PvpRecord> records;

		public DRUndoableEdit(final Collection<PvpRecord> recordsParam) {
			records = recordsParam;
		}

		@Override
		public void undo() throws CannotUndoException {
			context.data.getDataInterface().saveRecords(records);
		}

		@Override
		public boolean canUndo() {
			return true;
		}

		@Override
		public void redo() throws CannotRedoException {
			context.data.getDataInterface().deleteRecords(records);
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
			return "Delete Records";
		}

		@Override
		public String getUndoPresentationName() {
			return "Undo Delete Records";
		}

		@Override
		public String getRedoPresentationName() {
			return "Redo Delete Records";
		}

	}
}
