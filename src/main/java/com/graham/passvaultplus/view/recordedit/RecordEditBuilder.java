/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordedit;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.graham.util.DateUtil;
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
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0087");
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

			if (!f.equals(PvpField.CF_NOTES) &&
					!f.equals(PvpField.CF_CREATION_DATE) &&
					!f.equals(PvpField.CF_MODIFICATION_DATE)) {
				fieldNamesToDisplay.add(f.getName());
			}
		}

		fieldNamesToDisplay.add(PvpField.CF_NOTES.getName());

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

		JPanel p = new ScrollableRecordEdit(new GridBagLayout());

		for (int i = 0; i < fieldNamesToDisplay.size(); i++) {
			RecordEditFieldBuilder fieldBuilder = new RecordEditFieldBuilder(fieldNamesToDisplay.get(i), this);
			p.add(fieldBuilder.getLeftComponent(), labelConstraints);
			p.add(fieldBuilder.getRightComponent(), editorConstraints);
		}

		p.add(new JLabel(PvpField.CF_CATEGORY.getName() + ":", JLabel.RIGHT), labelConstraints);
		p.add(buildCategoryComponent(), editorConstraints);
		
		p.add(new JLabel(PvpField.CF_ARCHIVED_FLAG.getName() + ":", JLabel.RIGHT), labelConstraints);
		p.add(buildArchiveFlagComponent(), editorConstraints);

		if (!isNewRecord) {
			p.add(new JLabel(PvpField.CF_CREATION_DATE.getName() + ":", JLabel.RIGHT), labelConstraints);
			p.add(new JLabel(" " + DateUtil.formatDateTimeLocalized(record.getCreationDate())), editorConstraints);

			EmptyBorder border = new EmptyBorder(8, 0, 8, 0);
			JLabel mdlab = new JLabel(PvpField.CF_MODIFICATION_DATE.getName() + ":", JLabel.RIGHT);
			mdlab.setBorder(border);
			p.add(mdlab, labelConstraints);
			mdlab = new JLabel(" " + DateUtil.formatDateTimeLocalized(record.getModificationDate()));
			mdlab.setBorder(border);
			p.add(mdlab, editorConstraints);
		}

		JScrollPane scroll = new JScrollPane(p);
		scroll.getViewport().setBackground(p.getBackground());
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
		for (int i = 0; i < catList.size(); i++) {
			comboItems[i+1] = catList.get(i);
		}

		JComboBox catCombo = new JComboBox(comboItems);
		catCombo.setMaximumRowCount(20);
		RecordEditFieldCategory refc = new RecordEditFieldCategory(catCombo, editContext);
		editContext.editFields.put(PvpField.CF_CATEGORY.getName(), refc);
		refc.populateUIFromRecordField(record);

		catCombo.addActionListener(refc.afcAction);

		JPanel spacerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		spacerPanel.add(catCombo);
		return spacerPanel;
	}
	
	private Component buildArchiveFlagComponent() {
		JCheckBox check = new JCheckBox();
		RecordEditFieldArchiveFlag refa = new RecordEditFieldArchiveFlag(check, editContext);
		editContext.editFields.put(PvpField.CF_ARCHIVED_FLAG.getName(), refa);
		refa.populateUIFromRecordField(record);
		check.addActionListener(refa.afcAction);
		
		JPanel spacerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		spacerPanel.add(check);
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
