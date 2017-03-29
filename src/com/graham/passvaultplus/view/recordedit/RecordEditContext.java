/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.graham.passvaultplus.MyUndoManager;
import com.graham.passvaultplus.model.core.PvpRecord;

public class RecordEditContext {
	PvpRecord editRecord;
	SaveEditorAction saveAction;
	RevertEditorAction revertAction;
	Map<String, RecordEditField> editFields = new HashMap<>();
	JPanel panelInTabPane;
	JScrollPane centerPaneWithFields;
	JPanel maximizedTextAreaPanel;
	MyUndoManager undoManager;
	
	private boolean hasUnsavedChanges;
	private boolean ignoreAllChanges;

	public void populateRecordFromUI(final PvpRecord record) {
		for (final Map.Entry<String, RecordEditField> e1 : editFields.entrySet()) {
			e1.getValue().populateRecordFieldFromUI(record);
		}

		Date now = new Date();
		record.setModificationDate(now);
		if (record.getCreationDate() == null) {
			record.setCreationDate(now);
		}
	}

	public void populateUIFromRecord(final PvpRecord editRecordParam) {
		for (final Map.Entry<String, RecordEditField> e1 : editFields.entrySet()) {
			e1.getValue().populateUIFromRecordField(editRecordParam);
		}
	}
	
	public void populateUIFromRecord() {
		populateUIFromRecord(editRecord);
	}

	public int getRecordId() {
		return editRecord.getId();
	}

	public PvpRecord getRecord() {
		return editRecord;
	}

	public JPanel getPanelInTabPane() {
		return panelInTabPane;
	}

	public boolean hasUnsavedChanged() {
		return hasUnsavedChanges;
	}

	void setHasUnsavedChanges(final boolean b) {
		hasUnsavedChanges = b;
		revertAction.setEnabled(b);
		saveAction.setEnabled(b);
	}
	
	void setIgnoreAllChanges(boolean b) {
		// TODO this is not the best solution as it ignores all changes, may cause timing issues
		ignoreAllChanges = b;
		undoManager.setIgnoreAllChanges(b);
	}
	
	boolean shouldIngoreChanges() {
		return ignoreAllChanges;
	}
}
