/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEdit;

/**
 * There is an UndoManager provided by Swing, but we need additional functionality to handle the TabPane, and to coalesce events
 */
public class MyUndoManager implements UndoableEditListener, CaretListener {
	final static int MAX_UNDOS = 50;
	final public Action undoAction = new UndoAction();
	final public Action redoAction = new RedoAction();

	private Object lastEditSource;
	private int lastDot;
	private int currentUndoableEdit = -1;
	private ArrayList<MyUndoableEdit> undoableEdits = new ArrayList<>();
	private PvpContext pvpContext;
	private boolean ignoreAllChanges;

	public MyUndoManager(PvpContext contextParam) {
		pvpContext = contextParam;
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent e) {
		if (ignoreAllChanges) {
			return;
		}
		if (e.getSource() == lastEditSource) {
			//System.out.println("source SAME");
			undoableEdits.get(currentUndoableEdit).addEditEvent(e, pvpContext.getTabManager().getSelectedComponent());
		} else {
			//System.out.println("source DIFF");
			MyUndoableEdit ue = new MyUndoableEdit(e.getEdit(), pvpContext.getTabManager().getSelectedComponent());
			if (currentUndoableEdit < undoableEdits.size() - 1) {
				// if an undo has been done by the user, there are events after it that can be redone, we need to remove those now
				for (int i = undoableEdits.size() - 1; i > currentUndoableEdit; i--) {
					undoableEdits.remove(i);
				}
			}
			
			undoableEdits.add(ue);
			if (currentUndoableEdit > MAX_UNDOS) {
				undoableEdits.remove(0); // TODO test this
			} else {
				currentUndoableEdit++;
			}
			
			undoAction.setEnabled(canUndo());
			redoAction.setEnabled(canRedo());

			lastEditSource = e.getSource();
		}
	}
	
	public boolean canUndo() {
		return currentUndoableEdit >= 0;
	}
	
	public boolean canRedo() {
		return currentUndoableEdit < undoableEdits.size() - 1;
	}

	@Override
	public void caretUpdate(CaretEvent e) {
		// TODO the way lastEditSource is cleared need to be tested, like when user hits return, or they do a lot of typing
		if (Math.abs(e.getDot() - lastDot) > 1) {
			lastEditSource = null;
			//System.out.println("caretUpdate - reset:" + Math.abs(e.getDot() - lastDot));
		}
		lastDot = e.getDot();
	}
	
	public void notifyCloseTab(Component c) {
		for (int i = undoableEdits.size() - 1; i >= 0; i--) {
			if (undoableEdits.get(i).tabPane == c) {
				undoableEdits.remove(i);
				if (i <= currentUndoableEdit) {
					currentUndoableEdit--;
				}
			}
		}
		undoAction.setEnabled(canUndo());
		redoAction.setEnabled(canRedo());
	}
	
	public void setIgnoreAllChanges(boolean b) {
		ignoreAllChanges = b;
	}

	class UndoAction extends AbstractAction {
		public UndoAction() {
			super(null, PvpContext.getIcon("undo"));
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			MyUndoableEdit ue = undoableEdits.get(currentUndoableEdit);
			currentUndoableEdit--;
			pvpContext.getTabManager().setSelectedComponent(ue.tabPane);
			ue.undo();
			undoAction.setEnabled(canUndo());
			redoAction.setEnabled(canRedo());
		}
	}

	class RedoAction extends AbstractAction {
		public RedoAction() {
			super(null, PvpContext.getIcon("redo"));
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			MyUndoableEdit ue = undoableEdits.get(currentUndoableEdit + 1);
			currentUndoableEdit++;
			pvpContext.getTabManager().setSelectedComponent(ue.tabPane);
			ue.redo();
			undoAction.setEnabled(canUndo());
			redoAction.setEnabled(canRedo());
		}
	}
	
	static class MyUndoableEdit {
		private final ArrayList<UndoableEdit> edits = new ArrayList<>();
		private final Component tabPane;
		
		MyUndoableEdit(final UndoableEdit ueParam, final Component tabPaneParam) {
			edits.add(ueParam);
			tabPane = tabPaneParam;
		}
		
		void addEditEvent(final UndoableEditEvent evt, final Component tabPaneParam) {
			if (tabPane != tabPaneParam) {
				throw new RuntimeException("different tab");
			}
			edits.add(evt.getEdit());
		}
		
		void undo() {
			for (int i = edits.size() - 1; i >= 0; i--) {
				edits.get(i).undo();
			}
		}
		
		void redo() {
			for (int i = 0; i < edits.size(); i++) {
				edits.get(i).redo();
			}
		}
	}

}
