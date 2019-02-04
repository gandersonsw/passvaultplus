/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.view.MainFrame;
import com.graham.passvaultplus.view.OtherTab;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;
import com.graham.passvaultplus.view.recordlist.PvpViewListContext;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * Interface to Main UI. This is only available when app is initialized.
 */
public class PvpContextUIMainFrame {
	private final MyUndoManager undoManager;
	private final PvpContext context;
	MainFrame mainFrame;

	private Timer pinTimer;
	private PvpViewListContext viewListContext = new PvpViewListContext();
	private Map<OtherTab, Component> otherTabComps = new HashMap<>();

	private JTabbedPane mainTabPane = new JTabbedPane();
	private List<RecordEditContext> recordEditors = new ArrayList<>();

	PvpContextUIMainFrame(PvpContext c) {
		undoManager = new MyUndoManager(c);
		context = c;
		//	mainFrame = new MainFrame(c);
		schedulePinTimerTask();
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

	public void schedulePinTimerTask() {
		cancelPinTimerTask();
		if (context.prefs.getUsePin() && context.prefs.getPinTimeout() > 0) {
			pinTimer = new Timer();
			pinTimer.schedule(new PinTimerTask(context), context.prefs.getPinTimeout() * 60 * 1000);
		}
	}

	private void cancelPinTimerTask() {
		if (pinTimer != null) {
			pinTimer.cancel();
		}
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

	public void removeRecordEditor(final RecordEditContext r) {
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

}
