/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.*;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.actions.*;
import com.graham.passvaultplus.view.recordlist.ViewListBuilder;

public class MainFrame extends JFrame {

	//static final ButtonHiliteMouseListener buttonMouseoverListener = new ButtonHiliteMouseListener();

	class ThisWindAdapter extends java.awt.event.WindowAdapter {
		final QuitAction qa;
		public ThisWindAdapter(QuitAction qaParam) {
			qa = qaParam;
		}
		public void windowClosing(java.awt.event.WindowEvent event) {
			qa.actionPerformed(null);
		}
	}
	
	private static final long serialVersionUID = 6161486225628873141L;
	
	public MainFrame(final PvpContext context) {
		super("");
		setTitle("Pass Vault Plus");
		this.addWindowListener(new ThisWindAdapter(new QuitAction(context)));
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setLocation(100,100);
		setSize(904, 500);

		JTabbedPane tabPane = new JTabbedPane();
		context.getTabManager().setMainTabPane(tabPane);
		tabPane.add("Records", ViewListBuilder.buildViewList(context));

		JPanel toolBar = initToolBar(context);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(toolBar, BorderLayout.NORTH);
		mainPanel.add(tabPane, BorderLayout.CENTER);

		setContentPane(mainPanel);

		setVisible(true);
	}

	private JPanel initToolBar(final PvpContext context) {
		JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEADING));

		// TODO  test the error case where an exception is thrown from here

		//System.out.println("initToolBar:" + new File("strawberry.jpg").getAbsolutePath());

		JButton jbQuit = createImageButton(new QuitAction(context), KeyEvent.VK_Q);
		jbQuit.setToolTipText("[Q]uit");
		toolBar.add(jbQuit);

		JButton jbSettings = createImageButton(new ShowPrefsAction(context), KeyEvent.VK_COMMA);
		jbSettings.setToolTipText("[ , ] Settings");
		toolBar.add(jbSettings);

		JButton jbSchema = createImageButton(new SchemaEditorAction(), 0); // TODO
		jbSchema.setToolTipText("Schema Editor");
		jbSchema.setEnabled(false);
		toolBar.add(jbSchema);

		toolBar.add(Box.createHorizontalStrut(30));

		JButton jbNew = createImageButton(new NewRecordAction(context), KeyEvent.VK_N);
		jbNew.setToolTipText("Create [N]ew Record");
		toolBar.add(jbNew);

		JButton jbEdit = createImageButton(new EditRecordAction(context), KeyEvent.VK_E);
		jbEdit.setToolTipText("[E]dit Record");
		toolBar.add(jbEdit);

		JButton jbDelete = createImageButton(new DeleteRecord(context), KeyEvent.VK_DELETE);
		jbDelete.setToolTipText("[Delete] Selected Records");
		toolBar.add(jbDelete);

		toolBar.add(Box.createHorizontalStrut(30));

		JButton jbUndo = createImageButton(context.getUndoManager().undoAction, KeyEvent.VK_Z);
		jbUndo.setToolTipText("[Z] Undo");
		toolBar.add(jbUndo);

		JButton jbRedo = createImageButton(context.getUndoManager().redoAction, KeyEvent.VK_R);
		jbRedo.setToolTipText("[R]edo");
		toolBar.add(jbRedo);

		JButton jbCut = createImageButton(new CutAction(), 0); // the default cut keyshortcut works
		jbCut.setToolTipText("[X] Cut Selected Text");
		toolBar.add(jbCut);

		JButton jbCopy = createImageButton(new CopyAction(), 0); // the default copy keyshortcut works
		jbCopy.setToolTipText("[C]opy Selected Text");
		toolBar.add(jbCopy);

		JButton jbPaste = createImageButton(new PasteAction(), 0); // the default paste keyshortcut works
		jbPaste.setToolTipText("[V] Paste Text");
		toolBar.add(jbPaste);

		JButton jbSelectAll = createImageButton(new SelectAllAction(), 0); // the default selectAll keyshortcut works
		jbSelectAll.setToolTipText("Select [A]ll");
		toolBar.add(jbSelectAll);

		toolBar.add(Box.createHorizontalStrut(30));

		JButton jbHelp = createImageButton(new HelpAction(context), KeyEvent.VK_H);
		jbHelp.setToolTipText("[H]elp");
		toolBar.add(jbHelp);

		return toolBar;
	}

	/**
	 * @param a must not be null
	 * @param keyEvent if 0, no key shortcut is mapped
	 */
	private JButton createImageButton(final Action a, int keyEvent) {
		final JButton b = new JButton(a);
		b.setFocusable(false);
		if (keyEvent != 0) {
			final KeyStroke ks = KeyStroke.getKeyStroke(keyEvent, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
			final Object actionKey = "doit";
			b.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, actionKey);
			b.getActionMap().put(actionKey, a);
		}
		return b;
	}

}
