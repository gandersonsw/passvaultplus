/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.graham.passvaultplus.MyUndoManager;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;

public class RecordEditContext {
	PvpRecord editRecord;
	SaveEditorAction saveAction;
	RevertEditorAction revertAction;
	Map<String, JComponent> editFields = new HashMap<String, JComponent>();
	JPanel panelInTabPane;
	MyUndoManager undoManager;
	
	private boolean hasUnsavedChanges;
	private boolean ignoreAllChanges;

	public PvpRecord getSelectedCategory() {
		JComboBox catCombo = (JComboBox)editFields.get(PvpField.USR_CATEGORY);
		if (catCombo.getSelectedItem().equals(PvpRecord.NO_CATEGORY)) {
			return null;
		} else {
			return (PvpRecord)catCombo.getSelectedItem();
		}
	}

	public void setSelectedCategory(PvpRecord category) {
		JComboBox catCombo = (JComboBox) editFields.get(PvpField.USR_CATEGORY);
		if (category == null) {
			catCombo.setSelectedItem(PvpRecord.NO_CATEGORY);
		} else {
			catCombo.setSelectedItem(category);
		}
	}

	public void populateRecordFromUI(final PvpRecord record) {
		for (final Map.Entry<String, JComponent> e1 : editFields.entrySet()) {
			String name = e1.getKey();
			if (!name.equals(PvpField.USR_CATEGORY)) {
				String txt = ((JTextField)e1.getValue()).getText();
				record.setCustomField(name, txt);
			}
		}

		record.setCategory(getSelectedCategory());

		Date now = new Date();
		record.setModificationDate(now);
		if (record.getCreationDate() == null) {
			record.setCreationDate(now);
		}
	}

	public void populateUIFromRecord() {
		for (final Map.Entry<String, JComponent> e1 : editFields.entrySet()) {
			String name = e1.getKey();
			if (name.equals(PvpField.USR_CATEGORY)) {
				setSelectedCategory(editRecord.getCategory());
			} else {
				String val = editRecord.getCustomField(name);
				if (val == null) {
					val = "";
				}
				((JTextField) e1.getValue()).setText(val);
			}
		}

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
