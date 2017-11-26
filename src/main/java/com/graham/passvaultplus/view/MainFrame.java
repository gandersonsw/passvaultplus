/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.actions.*;
import com.graham.passvaultplus.model.core.PvpBackingStore;
import com.graham.passvaultplus.view.dashboard.DashBoardBuilder;
import com.graham.passvaultplus.view.recordlist.ViewListBuilder;

public class MainFrame extends JFrame {
	
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
		setSize(904, 520);
		setMinimumSize(new Dimension(400, 240));

		context.checkDashboard();
		context.getTabManager().addOtherTab("Records", ViewListBuilder.buildViewList(context));

		JPanel toolBar = initToolBar(context);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(toolBar, BorderLayout.NORTH);
		mainPanel.add(context.getTabManager().getMainTabPane(), BorderLayout.CENTER);
		mainPanel.add(initFooter(context), BorderLayout.SOUTH);

		setContentPane(mainPanel);

		setVisible(true);
	}
	
	private JPanel initStatusPanel(final PvpContext context) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		
		final JLabel info = context.getInfoLabel();
		info.setBorder(new EmptyBorder(3,3,7,10));
		p.add(info);
		
		for (PvpBackingStore bs : context.getFileInterface().getBackingStores()) {
			if (bs.isEnabled()) {
				final JLabel bsSN = new JLabel(bs.getShortName());
				bsSN.setFont(info.getFont());
				bsSN.setBorder(new EmptyBorder(3,8,7,3));
				p.add(bsSN);
				final StatusBox sb = new StatusBox(Color.GREEN);
				//sb.setBorder(new EmptyBorder(1,1,18,1));
				
				sb.addMouseListener(new MouseAdapter() {
			            @Override
			            public void mouseClicked(MouseEvent e) {
			              //  super.mouseClicked(e);
			                System.out.println("clicked");
			            }
			        });
				
				p.add(sb);
				bs.setStatusBox(sb);
			}
		}
		p.add(Box.createRigidArea(new Dimension(18, 0)));
		
		return p;
	}
	
	private JPanel initFooter(final PvpContext context) {
		final JPanel p = new JPanel(new BorderLayout());
		final JLabel logo = new JLabel(PvpContext.getIcon("pvplogo24pt"));
		logo.setBorder(new EmptyBorder(0,8,4,3));
		p.add(logo, BorderLayout.WEST);
		
		final JPanel ipanel = new JPanel(new BorderLayout());
		ipanel.add(initStatusPanel(context), BorderLayout.SOUTH);
		
		p.add(ipanel, BorderLayout.EAST);
		return p;
	}

	private JPanel initToolBar(final PvpContext context) {
		JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEADING));

		JButton jbQuit = createImageButton(new QuitAction(context), KeyEvent.VK_Q);
		jbQuit.setToolTipText("[Q]uit");
		toolBar.add(jbQuit);

		JButton jbSettings = createImageButton(new ShowPrefsAction(context), KeyEvent.VK_COMMA);
		jbSettings.setToolTipText("[ , ] Settings");
		toolBar.add(jbSettings);

		JButton jbSchema = createImageButton(new SchemaEditorAction(context), 0);
		jbSchema.setToolTipText("Schema Editor");
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
