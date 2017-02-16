/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.schemaedit;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpType;

public class PvpTypeModification {
	
	private int modID = 1;
	
	static class PvpFieldModification {
		String originalName;
		String newName;
		boolean isDeleted;
		boolean isNew;
		boolean isSecret;
		int id;
		JTextField tf;
		JCheckBox deletedCB;
		JCheckBox secretCB;
		PvpFieldModification(final PvpField f, final int idParam) {
			id = idParam;
			originalName = f.getName();
			newName = originalName;
			isSecret = f.isClassificationSecret();
		}
		PvpFieldModification(final int idParam) {
			id = idParam;
			originalName = "";
			newName = originalName;
			isNew = true;
		}
	}
	
	final private PvpType originalType;
	private List<PvpFieldModification> fieldMods = new ArrayList<>();
	private boolean isNewTypeFlag;
	JTextField newTypeNameTF; // only used for new types
	String newTypeNameString;
	
	JTextField toStringCodeTF;
	String toStringCodeString;
	
	JTextField fullFormatTF;
	String fullFormatString;
	
	PvpTypeModification() {
		// create a blank new type
		originalType = null;
		// create 1 empty field to get started
		fieldMods.add(new PvpFieldModification(modID++));
		isNewTypeFlag = true;
		newTypeNameString = "";
		toStringCodeString = "";
		fullFormatString = "";
	}
	
	PvpTypeModification(final PvpType originalTypeParam) {
		originalType = originalTypeParam;
		List<PvpField> fields = originalType.getFields();
		for (PvpField f : fields) {
			fieldMods.add(new PvpFieldModification(f, modID++));
		}
		isNewTypeFlag = false;
		toStringCodeString = originalType.getToStringCode();
		fullFormatString = originalType.getFullFormat();
	}
	
	List<PvpFieldModification> getFieldMods() {
		return fieldMods;
	}
	
	String getOriginalName() {
		return originalType.getName();
	}
	
	void addField(final int addAfterThisId) {
		if (addAfterThisId == 0) {
			fieldMods.add(0, new PvpFieldModification(modID++));
		} else {
			int foundIt = 0;
			for (int i = 0; i < fieldMods.size(); i++) {
				PvpFieldModification fm = fieldMods.get(i);
				if (fm.id == addAfterThisId) {
					fieldMods.add(i+1, new PvpFieldModification(modID++));
					foundIt++;
					break;
				}
			}
			System.out.println("addField:foundIt=" + foundIt);
		}
	}
	
	boolean isNewType() {
		return isNewTypeFlag;
	}
	
	
	void readUIValues() {
		List<PvpFieldModification> fmods = getFieldMods();
		for (PvpFieldModification fm : fmods) {
			fm.newName = fm.tf.getText().trim();
			fm.isDeleted = fm.deletedCB.isSelected();
			fm.isSecret = fm.secretCB.isSelected();
		}
		if (isNewType()) {
			newTypeNameString = newTypeNameTF.getText().trim();
		}
		
		toStringCodeString = toStringCodeTF.getText();
		fullFormatString = fullFormatTF.getText();
	}

}
