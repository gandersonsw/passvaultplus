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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.graham.passvaultplus.AppUtil;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.model.core.PvpType;

public class RecordEditBuilder {
	final PvpContext context;
	final PvpRecord record;
	final RecordEditContext editContext = new RecordEditContext();
	final boolean isNewRecord;
	final RightClickPopup rcPopup;

	public static RecordEditContext buildEditor(final PvpContext contextParam, final PvpRecord rParam, final boolean isNewRecordParam) {
		return new RecordEditBuilder(contextParam, rParam, isNewRecordParam).build();
	}

	public RecordEditBuilder(final PvpContext contextParam, final PvpRecord rParam, final boolean isNewRecordParam) {
		context = contextParam;
		record = rParam;
		editContext.editRecord = rParam;
		editContext.undoManager = context.uiMain.getUndoManager();
		isNewRecord = isNewRecordParam;
		rcPopup = new RightClickPopup(context);
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

		List<PvpField> typeFields = record.getType().getFields();
		List<String> fieldNamesToDisplay = new ArrayList<String>();

		for (int i = 0; i < typeFields.size(); i++) {
			PvpField f = typeFields.get(i);

			if (!f.getName().equals(PvpField.USR_NOTES) &&
					!f.getName().equals(PvpField.USR_CREATION_DATE) &&
					!f.getName().equals(PvpField.USR_MODIFICATION_DATE)) {
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

		for (int i = 0; i < fieldNamesToDisplay.size(); i++) {
			RecordEditFieldBuilder fieldBuilder = new RecordEditFieldBuilder(fieldNamesToDisplay.get(i), this);
			p.add(fieldBuilder.getLeftComponent(), labelConstraints);
			p.add(fieldBuilder.getRightComponent(), editorConstraints);
		}

		p.add(new JLabel(PvpField.USR_CATEGORY + ":", JLabel.RIGHT), labelConstraints);
		p.add(buildCategoryComponent(), editorConstraints);

		if (!isNewRecord) {
			p.add(new JLabel(PvpField.USR_CREATION_DATE + ":", JLabel.RIGHT), labelConstraints);
			p.add(new JLabel(AppUtil.formatDate1(record.getCreationDate())), editorConstraints);

			p.add(new JLabel(PvpField.USR_MODIFICATION_DATE + ":", JLabel.RIGHT), labelConstraints);
			p.add(new JLabel(AppUtil.formatDate1(record.getModificationDate())), editorConstraints);
		}

		JScrollPane scroll = new JScrollPane(p);
		editContext.centerPaneWithFields = scroll;
		return scroll;
	}


	private Component buildEditorBottom() {
		editContext.saveAction = new SaveEditorAction(context, editContext);
		editContext.saveAction.setEnabled(false);
		editContext.revertAction = new RevertEditorAction(editContext);
		editContext.revertAction.setEnabled(false);
		JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		if (record.getType().getFullFormat() != null) {
			p.add(new JButton(new CopyRecordFullFormatted(record)));
		}
		p.add(new JButton(editContext.revertAction));
		p.add(new JButton(new CancelEditorAction(context, editContext)));
		p.add(new JButton(editContext.saveAction));
		return p;
	}

	private Component buildCategoryComponent() {
		List<PvpRecord> catList = context.data.getDataInterface().getCategories();
		Object[] comboItems = new Object[catList.size() + 1];
		comboItems[0] = PvpRecord.NO_CATEGORY;
		int selectedIndex = 0;
		for (int i = 0; i < catList.size(); i++) {
			PvpRecord cat = catList.get(i);
			if (record.getCategory() != null && cat.getId() == record.getCategory().getId()) {
				selectedIndex = i + 1;
			}
			comboItems[i+1] = cat;
		}

		JComboBox catCombo = new JComboBox(comboItems);
		catCombo.setMaximumRowCount(20);
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
		List<PvpType> types = context.data.getDataInterface().getTypes();
		PvpType[] typeArray = new PvpType[types.size()];
		for (int i = 0; i < types.size(); i++) {
			typeArray[i] = types.get(i);
		}

		JComboBox<PvpType> typeCombo = new JComboBox<PvpType>(typeArray);
		typeCombo.setMaximumRowCount(20);
		typeCombo.setSelectedItem(record.getType());
		typeCombo.addActionListener(new NewRecordTypeChangedAction(context, editContext));
		return typeCombo;
	}
}
