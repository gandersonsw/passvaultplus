/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import javax.swing.text.JTextComponent;

import com.graham.passvaultplus.model.core.PvpRecord;

public class RecordEditFieldJTextComponent extends RecordEditField {
	
	final JTextComponent tc;
	final String fieldName;
	
	public RecordEditFieldJTextComponent(JTextComponent tcParam, final String fieldNameParam) {
		tc = tcParam;
		fieldName = fieldNameParam;
	}

	@Override
	public boolean isEdited(PvpRecord r) {
		String val = r.getCustomField(fieldName);
		if (val == null) {
			val = "";
		}
		return !val.equals(tc.getText());
	}

	@Override
	public void populateRecordFieldFromUI(PvpRecord r) {
		r.setCustomField(fieldName, tc.getText());
	}

	@Override
	public void populateUIFromRecordField(PvpRecord r) {
		String val = r.getCustomField(fieldName);
		if (val == null) {
			val = "";
		}
		tc.setText(val);
	}

	@Override
	public String getFieldTextForCopy() {
		return tc.getText();
	}
	
}
