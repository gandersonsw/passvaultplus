/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.graham.passvaultplus.AppUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.actions.TextFieldChangeForwarder;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.model.core.PvpType;

public class RecordEditBuilder {
	final private PvpContext context;
	final private PvpRecord r;
	final private RecordEditContext editContext = new RecordEditContext();
	final private boolean isNewRecord;

	public static RecordEditContext buildEditor(final PvpContext contextParam, final PvpRecord rParam, final boolean isNewRecordParam) {
		return new RecordEditBuilder(contextParam, rParam, isNewRecordParam).build();
	}

	public RecordEditBuilder(final PvpContext contextParam, final PvpRecord rParam, final boolean isNewRecordParam) {
		context = contextParam;
		r = rParam;
		editContext.editRecord = rParam;
		editContext.undoManager = context.getUndoManager();
		isNewRecord = isNewRecordParam;
	}

	private RecordEditContext build() {
		editContext.panelInTabPane = new JPanel(new BorderLayout());
		if (isNewRecord) {
			editContext.panelInTabPane.add(buildEditorChooseType(), BorderLayout.NORTH);
		}
		editContext.panelInTabPane.add(buildEditorTop(), BorderLayout.CENTER);
		editContext.panelInTabPane.add(buildEditorBottom(), BorderLayout.SOUTH);
		return editContext;
	}

	private Component buildEditorTop() {

		List<PvpField> typeFields = r.getType().getFields();
		List<String> fieldNamesToDisplay = new ArrayList<String>();

		for (int i = 0; i < typeFields.size(); i++) {
			PvpField f = typeFields.get(i);

			if (f.getName().equals(PvpField.USR_NOTES)) {

			} else if (f.getName().equals(PvpField.USR_CREATION_DATE)) {

			} else if (f.getName().equals(PvpField.USR_MODIFICATION_DATE)) {

			} else {
				fieldNamesToDisplay.add(f.getName());
			}
		}

		fieldNamesToDisplay.add(PvpField.USR_NOTES);

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.EAST ;
        labelConstraints.gridwidth = GridBagConstraints.RELATIVE;
        labelConstraints.fill = GridBagConstraints.NONE;
        labelConstraints.weightx = 0.0;

        GridBagConstraints editorConstraints = new GridBagConstraints();
        editorConstraints.anchor = GridBagConstraints.EAST ;
        editorConstraints.gridwidth = GridBagConstraints.REMAINDER;     //end row
        editorConstraints.fill = GridBagConstraints.HORIZONTAL;
        editorConstraints.weightx = 1.0;
      
		JPanel p = new JPanel(new GridBagLayout());
		EmptyBorder eBorder = new EmptyBorder(3,3,3,3);
		CompoundBorder cBorder = null;

		for (int i = 0; i < fieldNamesToDisplay.size(); i++) {
			String name = (String)fieldNamesToDisplay.get(i);
			JPanel leftPanel = new JPanel(new FlowLayout());
			leftPanel.add(new JLabel(name + ":", JLabel.RIGHT));
			JTextField tf = new JTextField(r.getCustomField(name));

			CopyFieldToClipboardAction copyAction = new CopyFieldToClipboardAction(PvpContext.getIcon("copy-small"));
			JButton copyButton = new JButton(copyAction);
			copyButton.setFocusable(false);
			leftPanel.add(copyButton);
			PvpField field = r.getType().getField(name);
			
			JComponent rightWidget = null;
			RecordEditField ref = null;
			if (field == null) {
				System.out.println("buildEditorTop: expected field to be not null:" + name); // TODO
			} else if (field.isClassificationSecret()) {
				if (!isNewRecord) {
					final RecordEditFieldSecret refs = new RecordEditFieldSecret(tf, name);
					ref = refs;
					JButton showSecretFieldButton = new JButton(new UnlockFieldAction(PvpContext.getIcon("unlock-small"), refs, editContext));
					showSecretFieldButton.setFocusable(false);
					showSecretFieldButton.setToolTipText("show value");
					rightWidget = showSecretFieldButton;
				}
			} else {
				rightWidget = buildFieldComboBox(name, tf);
			}
			
			if (ref == null) {
				ref = new RecordEditFieldJTextComponent(tf, name);
			}
			copyAction.setRecordEditField(ref);
			
			tf.getDocument().addUndoableEditListener(context.getUndoManager());
			tf.addCaretListener(context.getUndoManager());
			tf.getDocument().addDocumentListener(new TextFieldChangeForwarder(new AnyFieldChangedAction(editContext, ref)));
			
			p.add(leftPanel, labelConstraints);
			editContext.editFields.put(name, ref);
			if (cBorder == null) {
				cBorder = new CompoundBorder(eBorder, tf.getBorder());
			}
			tf.setBorder(cBorder);
			
			if (rightWidget != null) {
				JPanel p55 = new JPanel(new BorderLayout());
				p55.add(tf, BorderLayout.CENTER);
				p55.add(rightWidget, BorderLayout.EAST);
				p.add(p55, editorConstraints);
			} else {
				p.add(tf, editorConstraints);
			}
		}

		p.add(new JLabel(PvpField.USR_CATEGORY + ":", JLabel.RIGHT), labelConstraints);
		p.add(buildCategoryComponent(), editorConstraints);

		if (!isNewRecord) {
			p.add(new JLabel(PvpField.USR_CREATION_DATE + ":", JLabel.RIGHT), labelConstraints);
			p.add(new JLabel(AppUtil.formatDate1(r.getCreationDate())), editorConstraints);

			p.add(new JLabel(PvpField.USR_MODIFICATION_DATE + ":", JLabel.RIGHT), labelConstraints);
			p.add(new JLabel(AppUtil.formatDate1(r.getModificationDate())), editorConstraints);
		}

		JScrollPane scroll = new JScrollPane(p);

		return scroll;
	}

	private JComboBox buildFieldComboBox(String fieldName, JTextField tf) {
		List<String> values = context.getDataInterface().getCommonFiledValues(r.getType().getName(), fieldName);
		if (values.size() == 0) {
			return null;
		}
		String[] arr = new String[values.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = values.get(i);
		}
		JComboBox cb = new JComboBox<String>(arr);
		cb.setPrototypeDisplayValue("");
		cb.addActionListener (new TypeAheadListener(cb, tf));
		cb.setFocusable(false);
		return cb;
	}

	private Component buildEditorBottom() {
		editContext.saveAction = new SaveEditorAction(context, editContext);
		editContext.saveAction.setEnabled(false);
		editContext.revertAction = new RevertEditorAction(editContext);
		editContext.revertAction.setEnabled(false);
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		if (r.getType().getFullFormat() != null) {
			p.add(new JButton(new CopyRecordFullFormatted(r)));
		}
		p.add(new JButton(editContext.revertAction));
		p.add(new JButton(new CancelEditorAction(context, editContext)));
		p.add(new JButton(editContext.saveAction));
		return p;
	}

	private Component buildCategoryComponent() {
		List<PvpRecord> catList = context.getDataInterface().getCategories();
		Object[] comboItems = new Object[catList.size() + 1];
		comboItems[0] = PvpRecord.NO_CATEGORY;
		int selectedIndex = 0;
		for (int i = 0; i < catList.size(); i++) {
			PvpRecord cat = catList.get(i);
			if (r.getCategory() != null && cat.getId() == r.getCategory().getId()) {
				selectedIndex = i + 1;
			}
			comboItems[i+1] = cat;
		}

		JComboBox catCombo = new JComboBox(comboItems);
		RecordEditFieldCategory refc = new RecordEditFieldCategory(catCombo);
		editContext.editFields.put(PvpField.USR_CATEGORY, refc);
		catCombo.setSelectedIndex(selectedIndex);

		catCombo.addActionListener(new AnyFieldChangedAction(editContext, refc));

		JPanel spacerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		spacerPanel.add(catCombo);
		return spacerPanel;
	}

	private Component buildEditorChooseType() {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING));
		p.add(new JLabel("Type:"));
		p.add(buildTypeComboBox());
		return p;
	}

	private JComboBox<PvpType> buildTypeComboBox() {
		List<PvpType> types = context.getDataInterface().getTypes();
		PvpType[] typeArray = new PvpType[types.size()];
		for (int i = 0; i < types.size(); i++) {
			typeArray[i] = types.get(i);
		}

		JComboBox<PvpType> typeCombo = new JComboBox<PvpType>(typeArray);
		typeCombo.setSelectedItem(r.getType());
		typeCombo.addActionListener(new NewRecordTypeChangedAction(context, editContext));
		return typeCombo;
	}
}
