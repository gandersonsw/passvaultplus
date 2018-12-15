/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.prefs;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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

    context.ui.getMainFrame().setVisible(false);
    if (deleteDb.isSelected()) {
      System.out.println("deleteing stuff");
      PvpBackingStoreFile bsFile = new PvpBackingStoreFile(context.prefs);
      bsFile.deleteAll();
    }
    PvpBackingStoreGoogleDocs.deleteLocalCredentials();
    PvpPrefFacade.resetGlobalPrefs();
    PvpContext.startApp(false, null);
  }

  void doConfirmDialog() {
    d = new JDialog(null, "Confirm Reset", Dialog.ModalityType.APPLICATION_MODAL);
		d.getContentPane().setLayout(new BorderLayout());

		final JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		d.getContentPane().add(centerPanel, BorderLayout.CENTER);

		{
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
			final JPanel sp2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			sp2.add(new JLabel(" "));
			centerPanel.add(sp2);
		}

		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			p.add(new JButton(new CancelAction()));
			p.add(new JButton(new OkAction()));
			centerPanel.add(p);
		}

		d.pack();
		BCUtil.center(d);
		d.setResizable(false);
		d.setVisible(true); // this is the line that causes the dialog to Block
		//return actionHit;
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
