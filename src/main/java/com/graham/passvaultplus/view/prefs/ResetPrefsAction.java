/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.model.core.PvpBackingStoreFile;
import com.graham.passvaultplus.model.core.PvpBackingStoreGoogleDocs;
import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpPrefFacade;

public class ResetPrefsAction extends AbstractAction {

  private JDialog d;
  private boolean cancel = true;
  private JCheckBox deleteDb;

  private final PvpContext context;

	public ResetPrefsAction(final PvpContext contextParam) {
    super("Reset");
		context = contextParam;
	}

  public void actionPerformed(ActionEvent e) {
    doConfirmDialog();
    if (cancel) {
      return;
    }

    context.uiMain.getMainFrame().setVisible(false);
    if (deleteDb.isSelected()) {
      System.out.println("ResetPrefsAction.actionPerformed - deleteing stuff");
      PvpBackingStoreFile bsFile = new PvpBackingStoreFile(context.prefs.getDataFile());
      bsFile.deleteAll();
    }
    PvpBackingStoreGoogleDocs.deleteLocalCredentials();
    PvpPrefFacade.resetGlobalPrefs();
    PvpContext.startApp(false, null);
  }

  public void doConfirmDialog() {
		d = new JDialog(context.ui.getFrame(), "Confirm Reset", Dialog.ModalityType.APPLICATION_MODAL);
		d.getContentPane().setLayout(new BorderLayout());

		{
			ImageIcon icn = PvpContext.getIcon("option-pane-confirm", PvpContext.OPT_ICN_SCALE);
			JLabel icnLab = new JLabel(icn);
			icnLab.setBorder(new EmptyBorder(16, 25, 16, 24));
			JPanel p = new JPanel(new BorderLayout());
			p.add(icnLab, BorderLayout.NORTH);
			d.getContentPane().add(p, BorderLayout.WEST);
		}

		final JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		d.getContentPane().add(centerPanel, BorderLayout.CENTER);

		{
			final JPanel sp1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			sp1.add(new JLabel(" "));
			centerPanel.add(sp1);
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(new JLabel("Are you sure you want to reset all settings?\nThis will also delete local encryption keys."));
			centerPanel.add(p);
		}

		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			deleteDb = new JCheckBox("Delete Local Database Files Also. WARNING!!! THIS WILL DELETE THE ENTIRE LOCAL DATABASE");
			p.add(deleteDb);
			centerPanel.add(p);
		}

		{
			// add some spacers to make the button align to bottom and everything else to the top
			final JPanel sp1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			sp1.add(new JLabel(" "));
			centerPanel.add(sp1);
		}

		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			p.add(new JButton(new CancelAction()));
			p.add(new JButton(new OkAction()));
			p.setBorder(new EmptyBorder(4,20,10,16));
			centerPanel.add(p);
		}

		d.pack();
		BCUtil.center(d, context.ui.getFrame());
		d.setResizable(false);
		d.setVisible(true); // this is the line that causes the dialog to Block
  }

  class OkAction extends AbstractAction {
    OkAction() {
      super("Reset");
    }
    public void actionPerformed(ActionEvent e) {
      cancel = false;
      d.setVisible(false);
    }
  }

  class CancelAction extends AbstractAction {
    CancelAction() {
      super("Cancel");
    }
    public void actionPerformed(ActionEvent e) {
      cancel = true;
      d.setVisible(false);
    }
  }

}
