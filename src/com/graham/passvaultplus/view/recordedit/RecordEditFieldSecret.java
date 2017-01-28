package com.graham.passvaultplus.view.recordedit;

import javax.swing.text.JTextComponent;

import com.graham.passvaultplus.model.core.PvpRecord;

/**
 * Similar functionality to a text field, but it is more secure, so the value should not be shown as clear text by default.
 */
public class RecordEditFieldSecret extends RecordEditFieldJTextComponent {
	
	private String clearText; // the UI field may be "*******" so this has the real value, if isLocked is false, this value may not be up to date
	private boolean isLocked; // is the field locked, and the clearText value is hidden

	public RecordEditFieldSecret(JTextComponent tcParam, String fieldNameParam) {
		super(tcParam, fieldNameParam);
		setIsLockedAndHidden(true);
	}
	
	@Override
	public boolean isEdited(PvpRecord r) {
		String val = r.getCustomField(fieldName);
		if (val == null) {
			val = "";
		}
		if (isLocked) {
			return val.equals(clearText);
		}
		return val.equals(tc.getText());
	}

	@Override
	public void populateRecordFieldFromUI(PvpRecord r) {
		if (isLocked) {
			r.setCustomField(fieldName, clearText);
		} else {
			r.setCustomField(fieldName, tc.getText());
		}
	}

	@Override
	public void populateUIFromRecordField(PvpRecord r) {
		String val = r.getCustomField(fieldName);
		if (val == null) {
			val = "";
		}
		
		if (isLocked) {
			clearText = val;
		} else {
			tc.setText(val);
		}
	}
	
	public String getClearText() {
		return clearText;
	}
	
	public void setIsLockedAndHidden(final boolean isLockedParam) {
		isLocked = isLockedParam;
		if (isLocked) {
			clearText = tc.getText();
			tc.setText("******");
			tc.setEditable(false);
		} else {
			tc.setText(clearText);
			tc.setEditable(true);
		}
	}
	
	@Override
	public String getFieldTextForCopy() {
		if (isLocked) {
			return clearText;
		}
		return tc.getText();
	}

}
