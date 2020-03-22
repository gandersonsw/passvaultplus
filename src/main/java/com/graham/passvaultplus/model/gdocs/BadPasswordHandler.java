/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.gdocs;

import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.model.core.PvpBackingStore;
import com.graham.passvaultplus.model.core.PvpInStreamer;
import com.graham.passvaultplus.view.PwDialog;
import com.graham.passvaultplus.view.longtask.LTManager;
import com.graham.passvaultplus.view.longtask.LTRunner;
import com.graham.util.SwingUtil;

public class BadPasswordHandler {
	final PvpBackingStoreGoogleDocs bs;
	final PvpContext context;
	final PwDialog pd;
	String newPassword;
	JRadioButton useNewPw;
	JRadioButton useOldPw;

	JDialog d;

	public BadPasswordHandler(PvpBackingStoreGoogleDocs bsParam, final PvpContext contextParam) {
		bs = bsParam;
		context = contextParam;
		pd = new PwDialog();
		pd.setShowConfigButton(false);
		pd.setUseCancelInsteadOfQuit(true);
	}

	public void askForPw(boolean pwWasBad) {

			PwDialog.PwAction action = pd.askForPw(pwWasBad, "");
			if (action == PwDialog.PwAction.Cancel) {
				return;
			}

			newPassword = pd.getPw();

			PvpInStreamer pvpis = new PvpInStreamer(bs, context);

			LTManager.runWithProgress((ltr) -> {
				if (pvpis.validatePassword(ltr, newPassword)) {
					SwingUtilities.invokeLater(() -> doWhichPWDialog());
				} else {
					SwingUtilities.invokeLater(() -> askForPw(true));
				}
			}, "Verifying Password");
	}

	public void doWhichPWDialog() {
		d = new JDialog(context.uiMain.getMainFrame(), "Choose Password", Dialog.ModalityType.APPLICATION_MODAL);
		d.getContentPane().setLayout(new BoxLayout(d.getContentPane(), BoxLayout.Y_AXIS));
		{
			JLabel lbl = new JLabel("That password is correct. Which password should be used:");
			lbl.setBorder(new EmptyBorder(7,35,7,7));
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(lbl);
			d.getContentPane().add(p);
		}

		{
			useNewPw = new JRadioButton("Use the password that was just entered");
			useNewPw.setSelected(true);
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(useNewPw);
			d.getContentPane().add(p);
			final JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lbl =new JLabel("The other files will be upddated to use this new password");
			lbl.setBorder(new EmptyBorder(0,35,15,0));
			p2.add(SwingUtil.makeSmallLabel(lbl));
			d.getContentPane().add(p2);
		}

		{
			useOldPw = new JRadioButton("Use the existing password");
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.add(useOldPw);
			d.getContentPane().add(p);
			final JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel lbl = new JLabel("This file will be updated to use the old password.");
			lbl.setBorder(new EmptyBorder(0,35,30,0));
			p2.add(SwingUtil.makeSmallLabel(lbl));
			d.getContentPane().add(p2);
		}

		ButtonGroup group = new ButtonGroup();
		group.add(useNewPw);
		group.add(useOldPw);

		{
			final JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			final JButton okB = new JButton(new OkAction());
			p.add(okB);
			p.add(new JButton(new CancelAction()));
			d.getContentPane().add(p);
			d.getRootPane().setDefaultButton(okB);
		}

		d.pack();
		d.setLocationRelativeTo(context.uiMain.getMainFrame());
		d.setVisible(true); // this is the line that causes the dialog to Block
	}

	private void doLoad(LTRunner ltr, boolean useNewPassword) {
		try {
			PvpContext context2 = context.makeCopyWithPassword(newPassword);
			context2.prefs.setPassword(newPassword, false);
			context2.data.getFileInterface().loadcheckOneBackingStoreLT(context2, bs, false).runLongTask(ltr);

			if (useNewPassword) {
				context.prefs.setPassword(newPassword, context.prefs.isPasswordSaved());

				List<PvpBackingStore> bsList = context.data.getFileInterface().getEnabledBackingStoresWithUnmodifiedRemotes();
				for (PvpBackingStore bsi : bsList) {
					if (bsi != bs) {
						context.ui.notifyInfo("LongTaskLoadcheckOneBS :: setting dirty :: " + bsi.getClass().getName());
						bsi.setDirty(true);
					}
				}
			} else {
				bs.setDirty(true);
			}

		} catch (Exception e) {
			context.ui.notifyBadException(e, true, PvpException.GeneralErrCode.FallBackErr.OtherErr);
		}
	}

	class OkAction extends AbstractAction {
		OkAction() {
			super("Continue");
		}
		public void actionPerformed(ActionEvent e) {
			d.setVisible(false);
			final boolean useNewPassword = useNewPw.isSelected();
			LTManager.runWithProgress((ltr) -> doLoad(ltr, useNewPassword), "Synchronizing Passwords");
		}
	}

	class CancelAction extends AbstractAction {
		CancelAction() {
			super("Cancel");
		}
		public void actionPerformed(ActionEvent e) {
			d.setVisible(false);
		}
	}
}
