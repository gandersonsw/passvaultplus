/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.schemaedit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpType;
import com.graham.passvaultplus.view.OtherTab;
import com.graham.passvaultplus.view.OtherTabBuilder;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;

import com.graham.passvaultplus.model.search.SearchResults;

public class SchemaEditBuilder implements OtherTabBuilder {
	private PvpContext context;
	private SchemaChangesContext scContext;

	public String getTitle() {
		return "Schema";
	}

	public Component build(PvpContext c) {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0120");
		SchemaEditBuilder builder = new SchemaEditBuilder();
		builder.context = c;
		builder.scContext = new SchemaChangesContext();
		return builder.build();
	}

	public void dispose() {
	}

	private JPanel build() {
		scContext.panelInTabPane = new JPanel(new BorderLayout());
		scContext.panelInTabPane.add(buildSchemaTop(), BorderLayout.NORTH);
		return scContext.panelInTabPane;
	}

	private JPanel buildSchemaTop() {
		List<PvpType> types = context.data.getDataInterface().getTypes();

		scContext.typeCB = new JComboBox<>();
		scContext.typeCB.setMaximumRowCount(20);
		for (PvpType t : types) {
			scContext.typeCB.addItem(t);
		}

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(new JLabel("Type:"));
		p.add(scContext.typeCB);
		p.add(new JButton(new CreateNewTypeAction()));
		p.add(new JButton(new EditTypeAction()));
		p.add(new JButton(new DeleteTypeAction()));
		p.add(new JButton(new SaveChangesAction(scContext, context)));
		p.add(new JButton(new CloseSchemaEditAction()));

		return p;
	}

	private Component buildSchemaEditor() {
		List<PvpTypeModification.PvpFieldModification> fieldMods = scContext.tm.getFieldMods();
		final JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		{
			final JPanel typeP = new JPanel(new FlowLayout(FlowLayout.LEFT));
			if (scContext.tm.isNewType()) {
				typeP.add(new JLabel("Type Name: "));
				scContext.tm.newTypeNameTF = new JTextField(scContext.tm.newTypeNameString, 10);
				typeP.add(scContext.tm.newTypeNameTF);
			} else {
				typeP.add(new JLabel("Type: " + scContext.tm.getOriginalName()));
			}
			p.add(typeP);
		}

		{
			final JPanel firstAddP = new JPanel(new FlowLayout(FlowLayout.LEFT));
			firstAddP.add(new JButton(new AddFieldAction(0)));
			p.add(firstAddP);
		}

		for (PvpTypeModification.PvpFieldModification fm : fieldMods) {
			final JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

			fm.typeCB = new JComboBox<String>(PvpField.TYPES);
			fm.typeCB.setMaximumRowCount(20);
			addIfNotIncluded(fm.typeCB, fm.newType);
			fm.typeCB.setSelectedItem(fm.newType);
			fieldPanel.add(fm.typeCB);

			fm.tf = new JTextField(16);
			fm.tf.setText(fm.newName);
			fieldPanel.add(fm.tf);

			fm.secretCB = new JCheckBox("Secret");
			if (fm.isSecret) {
				fm.secretCB.setSelected(true);
			}
			fieldPanel.add(fm.secretCB);
			fm.deletedCB = new JCheckBox("Delete Field");
			if (fm.isDeleted) {
				fm.deletedCB.setSelected(true);
			}
			fieldPanel.add(fm.deletedCB);

			p.add(fieldPanel);

			final JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			addPanel.add(new JButton(new AddFieldAction(fm.id)));
			p.add(addPanel);
		}

		{
			final JPanel toStringP = new JPanel(new FlowLayout(FlowLayout.LEFT));
			toStringP.add(new JLabel("To String (short): "));
			scContext.tm.toStringCodeTF = new JTextField(scContext.tm.toStringCodeString, 10);
			toStringP.add(scContext.tm.toStringCodeTF);
			p.add(toStringP);
		}

		{
			final JPanel fullFormatP = new JPanel(new FlowLayout(FlowLayout.LEFT));
			fullFormatP.add(new JLabel("Full Format: "));
			scContext.tm.fullFormatTF = new JTextField(scContext.tm.fullFormatString, 30);
			fullFormatP.add(scContext.tm.fullFormatTF);
			p.add(fullFormatP);
		}

		JPanel pushToTopPanel = new JPanel(new BorderLayout());
		pushToTopPanel.add(p, BorderLayout.NORTH);
		JScrollPane sp = new JScrollPane(pushToTopPanel);
		return sp;
	}

	class CreateNewTypeAction extends AbstractAction {
		public CreateNewTypeAction() {
			super("Create New");
		}
		public void actionPerformed(ActionEvent e) {
			if (scContext.currentSchemaEditorPanel != null) {
				scContext.panelInTabPane.remove(scContext.currentSchemaEditorPanel);
			}
			scContext.tm = new PvpTypeModification();
			scContext.currentSchemaEditorPanel = buildSchemaEditor();
			scContext.panelInTabPane.add(scContext.currentSchemaEditorPanel, BorderLayout.CENTER);
			scContext.panelInTabPane.revalidate();
		}
	}

	class EditTypeAction extends AbstractAction {
		public EditTypeAction() {
			super("Edit");
		}
		public void actionPerformed(ActionEvent e) {
			if (scContext.currentSchemaEditorPanel != null) {
				scContext.panelInTabPane.remove(scContext.currentSchemaEditorPanel);
			}
			scContext.tm = new PvpTypeModification((PvpType)scContext.typeCB.getSelectedItem());
			scContext.currentSchemaEditorPanel = buildSchemaEditor();
			scContext.panelInTabPane.add(scContext.currentSchemaEditorPanel, BorderLayout.CENTER);
			scContext.panelInTabPane.revalidate();
		}
	}

	class DeleteTypeAction extends AbstractAction {
		public DeleteTypeAction() {
			super("Delete");
		}
		public void actionPerformed(ActionEvent e) {
			final PvpType type = (PvpType)scContext.typeCB.getSelectedItem();
			if (type == null) {
				return;
			}
			if (PvpDataInterface.TYPE_CATEGORY.equals(type.getName())) {
				context.ui.showMessageDialog("Delete", "Can't delete the Category type because it is used to structure all data.");
				return;
			}

			final SearchResults sr = context.data.getDataInterface().getFilteredRecords(type.getName(), "", null, false);
			final int recordCount = sr.records.size();
			final String message = "Are you sure you want to delete the type \"" + type.getName() + "\"?\n" + recordCount + " records will be deleted";

			boolean b = context.ui.showConfirmDialog("Confirm Delete Type", message);
			if (!b) {
				return;
			}

			closeTabsForType(type);

			context.data.deleteRecords(sr.getNestedRecords(), false); // false because we do an update a few lines below

			List<PvpType> types = context.data.getDataInterface().getTypes();
			types.remove(type);
			context.data.saveAndRefreshDataList();
			context.uiMain.getViewListContext().getTypeComboBox().removeItem(type);
			scContext.typeCB.removeItem(type);
		}

		private void closeTabsForType(final PvpType t) {
			List<RecordEditContext> toBeRemoved = new ArrayList<>();
			List<RecordEditContext> reList = context.uiMain.getRecordEditors();
			for (final RecordEditContext re : reList) {
				if (PvpType.sameType(re.getRecord().getType(), t)) {
					toBeRemoved.add(re);
				}
			}
			for (final RecordEditContext re : toBeRemoved) {
				context.uiMain.removeRecordEditor(re);
			}
		}
	}

	class AddFieldAction extends AbstractAction {
		final int addAfterThisId;
		public AddFieldAction(final int addAfterThisIdParam) {
			super("Add Field");
			addAfterThisId = addAfterThisIdParam;
		}
		public void actionPerformed(ActionEvent e) {
			scContext.tm.readUIValues();
			if (scContext.currentSchemaEditorPanel != null) {
				scContext.panelInTabPane.remove(scContext.currentSchemaEditorPanel);
			}
			scContext.tm.addField(addAfterThisId);
			scContext.currentSchemaEditorPanel = buildSchemaEditor();
			scContext.panelInTabPane.add(scContext.currentSchemaEditorPanel, BorderLayout.CENTER);
			scContext.panelInTabPane.revalidate();
		}
	}

	class CloseSchemaEditAction extends AbstractAction {
		public CloseSchemaEditAction() {
			super("Close");
		}
		public void actionPerformed(ActionEvent e) {
			context.uiMain.hideTab(OtherTab.SchemaEdit);
		}
	}

	private void addIfNotIncluded(JComboBox<String> cb, String s) {
		for (int i = 0; i < cb.getItemCount(); i++) {
			if (cb.getItemAt(i).equals(s)) {
				return;
			}
		}
		cb.addItem(s);
	}

}
