/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;

import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;

public class TabManager {
	private JTabbedPane mainTabPane = new JTabbedPane();
	private List<RecordEditContext> recordEditors = new ArrayList<>();
	private PvpContext context;

	public TabManager(final PvpContext contextParam) {
		context = contextParam;
	}

	public List<RecordEditContext> getRecordEditors() {
		return recordEditors;
	}

	public RecordEditContext getRecordEditor(final PvpRecord r) {
		for (RecordEditContext editor : recordEditors) {
			if (editor.getRecordId() == r.getId()) {
				return editor;
			}
		}
		return null;
	}

	public void addRecordEditor(final String tabLabel, final RecordEditContext r) {
		recordEditors.add(r);
		mainTabPane.add(tabLabel, r.getPanelInTabPane());
	}

	public void removeRecordEditor(final RecordEditContext r) {
		mainTabPane.remove(r.getPanelInTabPane());
		recordEditors.remove(r);
		context.ui.getUndoManager().notifyCloseTab(r.getPanelInTabPane());
	}

	/**
	 * Add a tab that is not a record editor.
	 */
	public void addOtherTab(final String tabLabel, final Component c) {
		mainTabPane.add(tabLabel, c);
	}

	/**
	 * remove a tab that is not a record editor.
	 */
	public void removeOtherTab(final Component c) {
		mainTabPane.remove(c);
		context.ui.getUndoManager().notifyCloseTab(c);
	}

	public boolean isCurrentTabList() {
		return mainTabPane.getSelectedIndex() == 0;
	}

	public RecordEditContext getCurrentTabRecordEditContext() {
		if (isCurrentTabList()) {
			return null;
		}
		Component c = mainTabPane.getSelectedComponent();

		for (RecordEditContext editor : recordEditors) {
			if (editor.getPanelInTabPane() == c) {
				return editor;
			}
		}

		// the preferences or help tab is selected
		return null;
	}

	public Component getSelectedComponent() {
		return mainTabPane.getSelectedComponent();
	}

	public void setSelectedComponent(final Component c) {
		mainTabPane.setSelectedComponent(c);
	}

	/**
	 * This should only be called by MainFrame
	 */
	public JTabbedPane getMainTabPane() {
		return mainTabPane;
	}
}
