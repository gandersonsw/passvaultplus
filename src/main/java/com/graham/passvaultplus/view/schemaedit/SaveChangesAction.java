/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.schemaedit;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.model.core.PvpType;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;
import com.graham.passvaultplus.view.schemaedit.PvpTypeModification.PvpFieldModification;

public class SaveChangesAction extends AbstractAction {

	final private SchemaChangesContext scContext;
	final private PvpContext context;
	private List<String> validationErrors = new ArrayList<>();

	public SaveChangesAction(final SchemaChangesContext scContextParam, final PvpContext c) {
		super("Save Changes");
		scContext = scContextParam;
		context = c;
	}

	public void actionPerformed(ActionEvent e) {
		if (scContext.tm == null) {
			return;
		}
		scContext.tm.readUIValues();

		if (scContext.tm.isNewType()) {
			createNewType();
		} else {
			changeExistingType();
		}

		if (!hasValidationErrors()) {
			scContext.panelInTabPane.remove(scContext.currentSchemaEditorPanel);

			final JPanel emptyPanel = new JPanel();
			scContext.currentSchemaEditorPanel = emptyPanel;
			scContext.panelInTabPane.add(emptyPanel, BorderLayout.CENTER);

			scContext.tm = null;
			scContext.panelInTabPane.revalidate();
		}
	}

	private void createNewType() {
		// ***** do validation ****
		clearValidationErrors();
		final Set<String> fieldsIncNew = new HashSet<>();

		final List<PvpFieldModification> fmods = scContext.tm.getFieldMods();
		for (PvpFieldModification fm : fmods) {
			if (fm.isDeleted) {
			} else {
				validateFieldName(fm.newName);
				if (fieldsIncNew.contains(fm.newName)) {
					addValidationError("field name is present more than once: " + fm.newName);
				}
				fieldsIncNew.add(fm.newName);
			}
		}
		validateTypeName(scContext.tm.newTypeNameString);
		validateFullFormat(fieldsIncNew, scContext.tm.fullFormatString);
		validateToStringCode(fieldsIncNew, scContext.tm.toStringCodeString);
		if (hasValidationErrors()) {
			showValidationErrors();
			return;
		}
		// ***** validation done ****

		final PvpType t = new PvpType();
		t.setName(scContext.tm.newTypeNameString);

		for (PvpFieldModification fm : fmods) {
			if (fm.isDeleted) {
				// do nothing because it doesn't exist yet
			} else {
				PvpField f = new PvpField(fm.newName, fm.newType, fm.isSecret ? PvpField.CLASSIFICATION_SECRET : null);
				t.getFields().add(f);
			}
		}

		t.setFullFormat(scContext.tm.fullFormatString);
		t.setToStringCode(scContext.tm.toStringCodeString);

		context.data.getDataInterface().getTypes().add(t);

		context.ui.getViewListContext().getTypeComboBox().addItem(t);
		scContext.typeCB.addItem(t);
		context.data.saveAndRefreshDataList();
	}

	private void changeExistingType() {
		final PvpType t = context.data.getDataInterface().getType(scContext.tm.getOriginalName());
		final boolean isCatType = PvpType.sameType(t, PvpDataInterface.TYPE_CATEGORY);

		// ***** do validation ****
		clearValidationErrors();
		if (t == null) {
			JOptionPane.showMessageDialog(context.ui.getMainFrame(), "Error: type not found in database: " + scContext.tm.getOriginalName());
			return;
		}
		final Set<String> fieldsIncNew = new HashSet<>();

		final List<PvpFieldModification> fmods = scContext.tm.getFieldMods();
		for (PvpFieldModification fm : fmods) {
			if (fm.isDeleted) {
				// check for how many records have this field
			} else {
				validateFieldName(fm.newName);
				if (fieldsIncNew.contains(fm.newName)) {
					addValidationError("field name is present more than once: " + fm.newName);
				}
				fieldsIncNew.add(fm.newName);
			}
		}
		validateFullFormat(fieldsIncNew, scContext.tm.fullFormatString);
		validateToStringCode(fieldsIncNew, scContext.tm.toStringCodeString);
		if (hasValidationErrors()) {
			showValidationErrors();
			return;
		}

		if (!closeTabsForType(t)) {
			addValidationError("there was unsaved records"); // just do this so the UI is not closed
			return;
		}
		// ***** validation done ****

		final PvpDataInterface.FilterResults fr = context.data.getDataInterface().getFilteredRecords(t.getName(), "", null, false);

		for (PvpFieldModification fm : fmods) {
			if (fm.isDeleted) {
				final PvpField f = t.getField(fm.originalName);
				if (f == null) {
					// this can happen if they add a new field, and then select delete
				} else if (isCatType && fm.originalName.equals(PvpField.USR_CATEGORY_TITLE)) {
					context.ui.notifyWarning("can't delete Title field in Category type");
				} else {
					for (PvpRecord r : fr.records) {
						r.setCustomField(fm.originalName, null);
					}
					t.getFields().remove(f);
				}
			} else if (fm.isNew) {
				PvpField f = new PvpField(fm.newName, fm.newType, fm.isSecret ? PvpField.CLASSIFICATION_SECRET : null);
				t.getFields().add(f); // TODO handle the order
			} else if (isCatType && fm.originalName.equals(PvpField.USR_CATEGORY_TITLE)) {
				if (!fm.newName.equals(fm.originalName)) {
					context.ui.notifyWarning("can't change Title field in Category type");
				}
			} else {
				final PvpField f = t.getField(fm.originalName);
				if (!fm.newName.equals(fm.originalName)) {
					for (PvpRecord r : fr.records) {
						final String val = r.getCustomField(fm.originalName);
						r.setCustomField(fm.originalName, null);
						r.setCustomField(fm.newName, val);
					}
					f.setName(fm.newName);
				}
				if (fm.isSecret != f.isClassificationSecret()) {
					f.setClassification(fm.isSecret ? PvpField.CLASSIFICATION_SECRET : null);
				}
				f.setType(fm.newType);
			}
		}

		t.setFullFormat(scContext.tm.fullFormatString);
		t.setToStringCode(scContext.tm.toStringCodeString);

		context.data.saveAndRefreshDataList();
	}

	private void addValidationError(final String errMsg) {
		if (!validationErrors.contains(errMsg)) { // dont duplicate the same message
			validationErrors.add(errMsg);
		}
	}

	private void clearValidationErrors() {
		validationErrors = new ArrayList<>();
	}

	private boolean hasValidationErrors() {
		return validationErrors.size() > 0;
	}

	private void showValidationErrors() {
		if (validationErrors.size() == 0) {
			return;
		} else if (validationErrors.size() == 1) {
			JOptionPane.showMessageDialog(context.ui.getMainFrame(), validationErrors.get(0));
		} else {
			StringBuilder sb = new StringBuilder("There were errors that prevented the type from saving:\n\n");
			for (String e : validationErrors) {
				sb.append(e);
				sb.append("\n");
			}
			JOptionPane.showMessageDialog(context.ui.getMainFrame(), sb.toString());
		}
	}

	private void validateFieldName(final String name) {
		if (name.length() == 0) {
			addValidationError("field name is required for all fields");
			return;
		}

		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (Character.isLetterOrDigit(c)) {
			} else if (c == ' ') {
			} else {
				addValidationError("character not allowed in field name: " + c);
			}
		}
	}

	private void validateTypeName(final String name) {
		if (name.length() == 0) {
			addValidationError("type name is required");
			return;
		}

		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			if (Character.isLetterOrDigit(c)) {
			} else if (c == ' ') {
				addValidationError("character not allowed in type name: space");
			} else {
				addValidationError("character not allowed in type name: " + c);
			}
		}
	}

	private void validateToStringCode(final Set<String> fieldsIncNew, final String toStringCodeString) {
		if (toStringCodeString.length() == 0) {
			addValidationError("the field \"To String\" is required");
			return;
		}

		if (!fieldsIncNew.contains(toStringCodeString)) {
			addValidationError("the field \"To String\" must be one of the fields");
		}
	}

	private void validateFullFormat(final Set<String> fieldsIncNew, final String fullFormatString) {
		// nothing to do here
	}

	private boolean closeTabsForType(final PvpType t) {
		List<RecordEditContext> toBeRemoved = new ArrayList<>();
		List<RecordEditContext> reList = context.ui.getTabManager().getRecordEditors();
		int userChoice = -1;
		for (final RecordEditContext re : reList) {
			if (PvpType.sameType(re.getRecord().getType(), t)) {
				if (userChoice == -1 && re.hasUnsavedChanged()) {
					userChoice = JOptionPane.showConfirmDialog(context.ui.getMainFrame(), "There are unsaved records of this type, do you want to continue?", "Unsaved Data", JOptionPane.OK_CANCEL_OPTION);
					if (userChoice == JOptionPane.CANCEL_OPTION) {
						return false;
					}
				}
				toBeRemoved.add(re);
			}
		}

		for (final RecordEditContext re : toBeRemoved) {
			context.ui.getTabManager().removeRecordEditor(re);
		}

		return true;
	}

}
