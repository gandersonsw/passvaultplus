/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.recordlist;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.model.RecordFilter;
import com.graham.passvaultplus.model.RecordListViewOptions;
import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpType;
import com.graham.util.GenUtil;

public class RecordListTableMouseAdapter extends MouseAdapter {
	final PvpContext context;
	final AbstractAction doubleClickAction;
	final RecordFilter filter;

	JPopupMenu popup;
	JMenu sortMenu;
	PvpType sortMenuType; // the type that the sort menu has the fields listed for
	JCheckBoxMenuItem currentVertTableLayoutMI;
	JCheckBoxMenuItem currentSortMI;

	public RecordListTableMouseAdapter(final PvpContext contextParam, final AbstractAction actionParam, final RecordFilter filterParam) {
		context = contextParam;
		doubleClickAction = actionParam;
		filter = filterParam;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		showPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		showPopup(e);
	}

	private void showPopup(MouseEvent e) {
		com.graham.passvaultplus.PvpContextUI.checkEvtThread("3524");
		if (e.isPopupTrigger()) {
			getPopupMenu().show(e.getComponent(), e.getX(), e.getY());
			return;
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			doubleClickAction.actionPerformed(null);
		}
	}

	JPopupMenu getPopupMenu() {
		if (popup != null) {
			checkSortMenuType();
			return popup;
		}
		com.graham.passvaultplus.PvpContextUI.checkEvtThread("4536");
		popup = new JPopupMenu("Options");
		popup.add(new ToggleFieldVisibilityAction(PvpField.CF_CREATION_DATE).createMI());
		popup.add(new ToggleFieldVisibilityAction(PvpField.CF_MODIFICATION_DATE).createMI());
		popup.add(new ToggleFieldVisibilityAction(PvpField.CF_TYPE).createMI());
		popup.add(new ToggleFieldVisibilityAction(PvpField.CF_CATEGORY).createMI());
		popup.add(new ToggleFieldVisibilityAction(PvpField.CF_NOTES).createMI());

		JMenu vertOpts = new JMenu("Vertical Table Layout");
		vertOpts.add(new VertTableLayoutAction("Never", RecordListViewOptions.VTL_NEVER).createMI());
		vertOpts.add(new VertTableLayoutAction("Only 1 record", RecordListViewOptions.VTL_ONLY_1).createMI());
		vertOpts.add(new VertTableLayoutAction("Less than 5 records", RecordListViewOptions.VTL_LT_5).createMI());
		vertOpts.add(new VertTableLayoutAction("More than 1 data type", RecordListViewOptions.VTL_GT_1_TYPE).createMI());
		vertOpts.add(new VertTableLayoutAction("Always", RecordListViewOptions.VTL_ALWAYS).createMI());
		popup.add(vertOpts);

		sortMenu = new JMenu("Sorting");
		currentSortMI = new SortAction(PvpField.CF_SEARCH_MATCH).createMI();
		currentSortMI.setSelected(true);
		sortMenu.add(currentSortMI);
		sortMenu.add(new SortAction(PvpField.CF_IDENTITY).createMI());
		sortMenu.add(new SortAction(PvpField.CF_CATEGORY).createMI());
		sortMenu.add(new SortAction(PvpField.CF_TYPE).createMI());
		sortMenu.add(new SortAction(PvpField.CF_CREATION_DATE).createMI());
		sortMenu.add(new SortAction(PvpField.CF_MODIFICATION_DATE).createMI());
		sortMenu.add(new SortAction(PvpField.CF_VIRTUAL_SUMMARY).createMI());
		popup.add(sortMenu);

		checkSortMenuType();

		return popup;
	}

	private void checkSortMenuType() {
		if (GenUtil.equalsWithNull(filter.getTypeIfAllSame(), sortMenuType)) {
			return;
		}

		JMenuItem saveSortItem = null;
		while (sortMenu.getItemCount() > 6) {
			if (sortMenu.getItem(6).isSelected()) {
				saveSortItem = sortMenu.getItem(6);
			}
			sortMenu.remove(6);
		}
		if (saveSortItem != null) {
			sortMenu.add(saveSortItem);
		}

		sortMenuType = filter.getTypeIfAllSame();
		if (sortMenuType == null) {
			return;
		}
		for (PvpField f : sortMenuType.getFields()) {
			sortMenu.add(new SortAction(f).createMI());
		}
	}

	class ToggleFieldVisibilityAction extends AbstractAction {
		JCheckBoxMenuItem menuItem;
		PvpField field;
		public ToggleFieldVisibilityAction(PvpField fieldParam) {
			super("Show " + fieldParam.getName());
			field = fieldParam;
		}
		JMenuItem createMI() {
			menuItem = new JCheckBoxMenuItem(this);
			menuItem.setSelected(context.prefs.getRecordListViewOptions().getFieldVisible(field));
			return menuItem;
		}
		public void actionPerformed(ActionEvent e) {
			menuItem.setSelected(context.prefs.getRecordListViewOptions().toggleFieldVisible(field));
			context.uiMain.getViewListContext().filterUIChanged();
		}
	}

	class VertTableLayoutAction extends AbstractAction {
		int vertTableLayout;
		JCheckBoxMenuItem menuItem;
		public VertTableLayoutAction(String label, int vertTableLayoutParam) {
			super(label);
			vertTableLayout = vertTableLayoutParam;
		}
		JMenuItem createMI() {
			menuItem = new JCheckBoxMenuItem(this);
			if (context.prefs.getRecordListViewOptions().getVertTableLayout() == vertTableLayout) {
				menuItem.setSelected(true);
				currentVertTableLayoutMI = menuItem;
			}
			return menuItem;
		}
		public void actionPerformed(ActionEvent e) {
			if (!menuItem.equals(currentVertTableLayoutMI)) {
				currentVertTableLayoutMI.setSelected(false);
				menuItem.setSelected(true);
				currentVertTableLayoutMI = menuItem;
				context.prefs.getRecordListViewOptions().setVertTableLayout(vertTableLayout);
				context.uiMain.getViewListContext().filterUIChanged();
			}
		}
	}

	class SortAction extends AbstractAction {
		PvpField field;
		JCheckBoxMenuItem menuItem;
		public SortAction(PvpField fieldParam) {
			super(fieldParam.getName());
			field = fieldParam;
		}
		JCheckBoxMenuItem createMI() {
			menuItem = new JCheckBoxMenuItem(this);
			if (field.getName().equals(context.prefs.getRecordListViewOptions().getSort())) {
				menuItem.setSelected(true);
				currentSortMI = menuItem;
			}
			return menuItem;
		}
		public void actionPerformed(ActionEvent e) {
			if (menuItem.equals(currentSortMI)) {
				int newDir = context.prefs.getRecordListViewOptions().getSortDir() == RecordListViewOptions.SORT_ASC ? RecordListViewOptions.SORT_DESC : RecordListViewOptions.SORT_ASC;
				mySelect(newDir);
				context.uiMain.getViewListContext().filterUIChanged();
			} else {
				myUnselect(currentSortMI);
				mySelect(RecordListViewOptions.SORT_ASC);
				currentSortMI = menuItem;
				context.uiMain.getViewListContext().filterUIChanged();
			}
		}
		private void myUnselect(JCheckBoxMenuItem mi) {
			if (mi != null) {
				mi.setSelected(false);
				SortAction sa = (SortAction) mi.getAction();
				mi.setText(sa.field.getName());
			}
		}
		private void mySelect(int sortDir) {
			menuItem.setSelected(true);
			String dirText = sortDir == RecordListViewOptions.SORT_ASC ? " (Ascending)" : " (Descending)";
			menuItem.setText(field.getName() + dirText);
			context.prefs.getRecordListViewOptions().setSort(sortMenuType, field, sortDir);
		}
	}

}


