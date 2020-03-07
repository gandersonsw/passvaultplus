/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.view.MainFrame;
import com.graham.passvaultplus.view.OtherTab;
import com.graham.passvaultplus.view.recordedit.RecordEditBuilder;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;
import com.graham.passvaultplus.view.recordlist.PvpViewListContext;
import com.graham.util.AppUtil;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/**
 * Interface to Main UI. This is only available when app is initialized.
 */
public class PvpContextUIMainFrame implements ChangeListener {
	private final MyUndoManager undoManager;
	private final PvpContext context;
	MainFrame mainFrame;

	private PvpViewListContext viewListContext = new PvpViewListContext();
	private Map<OtherTab, Component> otherTabComps = new HashMap<>();

	private JTabbedPane mainTabPane = new JTabbedPane();
	private List<RecordEditContext> recordEditors = new ArrayList<>();

	private Component lastSelectedComp1;
	private Component lastSelectedComp2;

	PvpContextUIMainFrame(PvpContext c) {
		undoManager = new MyUndoManager(c);
		context = c;
		mainTabPane.addChangeListener(this);
		checkmergeDelRecs();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		lastSelectedComp2 = lastSelectedComp1;
		lastSelectedComp1 = mainTabPane.getSelectedComponent();
	}

	public void showTab(OtherTab t) {
		try {
			if (otherTabComps.get(t) == null) {
				Component c = t.getBuilder().build(context);
				otherTabComps.put(t, c);
				mainTabPane.add(t.getBuilder().getTitle(), c);
			}
			if (t.isSelectedWhenShown()) {
				setSelectedComponent(otherTabComps.get(t));
			}
		} catch (Exception e) {
			context.ui.notifyWarning("Error creating tab:" + t, e);
		}
	}

	public void hideTab(OtherTab t) {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0242");
		if (otherTabComps.get(t) != null) {
			mainTabPane.remove(otherTabComps.get(t));
			context.uiMain.getUndoManager().notifyCloseTab(otherTabComps.get(t));
			otherTabComps.remove(t);
			t.getBuilder().dispose();
		}
	}

	public void setTabVisible(OtherTab t, boolean v) {
		if (v) {
			showTab(t);
		} else {
			hideTab(t);
		}
	}

	public PvpViewListContext getViewListContext() {
		return viewListContext;
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	public MyUndoManager getUndoManager() {
		return undoManager;
	}

	/**
	 * These are the tabs set be the preferences.
	 */
	public void checkOtherTabs() {
		setTabVisible(OtherTab.Dashboard, context.prefs.getShowDashboard());
		setTabVisible(OtherTab.Diagnostics, context.prefs.getShowDiagnostics());
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

	public void addRecordEditorIfNeeded(PvpRecord rec) {
		if (rec != null) {
			RecordEditContext editor = getRecordEditor(rec);
			if (editor == null) {
				editor = RecordEditBuilder.buildEditor(context, rec, false);
				addRecordEditor(AppUtil.limitStrLen(rec.toString(), 30), editor);
			}
			setSelectedComponent(editor.getPanelInTabPane());
		}
	}

	public void removeRecordEditor(final RecordEditContext r) {
		setLastTabSelected();
		mainTabPane.remove(r.getPanelInTabPane());
		recordEditors.remove(r);
		context.uiMain.getUndoManager().notifyCloseTab(r.getPanelInTabPane());
	}

	public boolean isCurrentTabList() {
		return mainTabPane.getSelectedIndex() == 0;
	}

	public RecordEditContext getCurrentTabRecordEditContext() {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0244");
		if (isCurrentTabList()) {
			return null;
		}
		Component c = mainTabPane.getSelectedComponent();
		for (RecordEditContext editor : recordEditors) {
			if (editor.getPanelInTabPane() == c) {
				return editor;
			}
		}
		// one of the OtherTab is selected
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

	private void setLastTabSelected() {
		if (lastSelectedComp2 != null) {
			if (mainTabPane.indexOfComponent(lastSelectedComp2) >= 0) {
				mainTabPane.setSelectedComponent(lastSelectedComp2);
			}
			lastSelectedComp2 = null;
		}
	}

	// TODO delete this after tested better
	private void checkmergeDelRecs() {
			for (List<com.graham.passvaultplus.model.core.PvpDataMerger.DelRec> a : context.mergeDelRecs) {
					createDelRecTab(a);
			}
	}

	// TODO delete this after tested better
	public void createDelRecTab(List<com.graham.passvaultplus.model.core.PvpDataMerger.DelRec> a) {
			JPanel p = new JPanel(new GridLayout(a.size(), 1));
			for (com.graham.passvaultplus.model.core.PvpDataMerger.DelRec b : a) {
					JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
					p2.add(new JButton(new DelAction(b.rec)));
					p2.add(new JLabel(b.inFromDb ? "X" : "_"));
					p2.add(new JLabel(String.valueOf(b.rec.getId())));
					p2.add(new JLabel(b.rec.getDebugText(false)));
					p.add(p2);
			}
			JScrollPane sp = new JScrollPane(p);
			mainTabPane.add("Del Recs", sp);
	}

	class DelAction extends AbstractAction {
			PvpRecord rec;
			DelAction(PvpRecord r) {
					super("Add Back");
					rec = r;
			}
			@Override
			public void actionPerformed(ActionEvent e) {
					rec.clearId();
					context.data.getDataInterface().saveRecord(rec);
					JButton b = (JButton)e.getSource();
					b.setEnabled(false);
			}
	}

}
