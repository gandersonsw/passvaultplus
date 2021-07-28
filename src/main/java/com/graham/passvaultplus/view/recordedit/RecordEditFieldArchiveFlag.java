/* Copyright (C) 2021 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import javax.swing.JCheckBox;

import com.graham.passvaultplus.PvpContextUI;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.util.GenUtil;

public class RecordEditFieldArchiveFlag extends RecordEditField {

	private JCheckBox check;

	public RecordEditFieldArchiveFlag(final JCheckBox c) {
		check = c;
	}

	@Override
	public boolean isEdited(PvpRecord r) {
		return r.isArchived() != check.isSelected();
	}

	@Override
	public void populateRecordFieldFromUI(PvpRecord r) {
    r.setArchived(check.isSelected());
	}

	@Override
	public void populateUIFromRecordField(PvpRecord r) {
    check.setSelected(r.isArchived());
	}

	@Override
	public String getFieldTextForCopy() {
		PvpContextUI.getActiveUI().notifyWarning("RecordEditFieldArchiveFlag.getFieldTextForCopy :: should not be called");
		return "";
	}

}
