/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import javax.swing.JComboBox;

import com.graham.passvaultplus.AppUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpRecord;

public class RecordEditFieldCategory extends RecordEditField {
	
	private JComboBox catCombo;
	
	public RecordEditFieldCategory(final JComboBox catComboParam) {
		catCombo = catComboParam;
	}

	@Override
	public boolean isEdited(PvpRecord r) {
		return AppUtil.equalsWithNull(getSelectedCategory(), r.getCategory());
	}
	
	private PvpRecord getSelectedCategory() {
		if (catCombo.getSelectedItem().equals(PvpRecord.NO_CATEGORY)) {
			return null;
		} else {
			return (PvpRecord)catCombo.getSelectedItem();
		}
	}
	
	private void setSelectedCategory(PvpRecord category) {
		if (category == null) {
			catCombo.setSelectedItem(PvpRecord.NO_CATEGORY);
		} else {
			catCombo.setSelectedItem(category);
		}
	}

	@Override
	public void populateRecordFieldFromUI(PvpRecord r) {
		r.setCategory(getSelectedCategory());
	}

	@Override
	public void populateUIFromRecordField(PvpRecord r) {
		setSelectedCategory(r.getCategory());
	}

	@Override
	public String getFieldTextForCopy() {
		PvpContext.getActiveContext().notifyWarning("RecordEditFieldCategory.getFieldTextForCopy :: should not be called");
		return "";
	}

}
