/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.graham.passvaultplus.PvpContext;
import com.graham.passvaultplus.PvpContextUI;
import com.graham.passvaultplus.PvpException;
import com.graham.passvaultplus.actions.GoToUrlAction;
import com.graham.util.GenUtil;
import com.graham.util.SwingUtil;

public class ErrorFrame {
	
	public final static String PVP_HELP_URL_ERR_BASE = "http://passvaultplus.com/help/errors/";
	
	private JTextArea detailsText;
	private JScrollPane detailsScroll;
	private int badExceptionMessageCount;
	private JFrame eFrame;
	private JButton showDetails;
	private JTextArea helpLink;
	
	public void notify(final Exception e, final boolean canContinue, final boolean canGoToSetup, final boolean canQuit, final PvpException.GeneralErrCode gErrCode, final StringBuilder warnings) {

			if (SwingUtilities.isEventDispatchThread()) {
					notifyInternal(e, canContinue, canGoToSetup, canQuit, gErrCode, warnings);
			} else {
					try {
							SwingUtilities.invokeAndWait(() -> notifyInternal(e, canContinue, canGoToSetup, canQuit, gErrCode, warnings));
					} catch (Exception excep) {
							PvpContextUI.getActiveUI().notifyWarning("ErrorFrame.notify", excep);
					}
			}
	}


	private void notifyInternal(final Exception e, final boolean canContinue, final boolean canGoToSetup, final boolean canQuit, final PvpException.GeneralErrCode gErrCode, final StringBuilder warnings) {


				//com.graham.passvaultplus.PvpContextUI.getActiveUI().notifyInfo("check thread 151");
			com.graham.passvaultplus.PvpContextUI.getActiveUI().checkEvtThread("3029");
		if (detailsText != null) {
			if (badExceptionMessageCount < 10) {
				detailsText.append("\n\n" + e.getMessage() + "\n\n" + GenUtil.getExceptionStackTrace(e));
			}
			badExceptionMessageCount++;
			return;
		}
		
		// if the exception is a PvpException, use the more specific data in there
		// but dont look into the nested exception.
		String errTitle;
		String errDesc;
		String errHelpId;
		Action optionalAction = null;
		if (e instanceof PvpException) {
			final PvpException pvpe = (PvpException)e;
			errTitle = pvpe.getPvpErrorTitle();
			errDesc = pvpe.getPvpErrorDescription();
			errHelpId = pvpe.getPvpHelpId();
			optionalAction = pvpe.getPvpOptionalAction();
		} else if (gErrCode != null) {
			errTitle = gErrCode.getTitle();
			errDesc = gErrCode.getDescription();
			errHelpId = gErrCode.getHelpId();
		} else {
			errTitle = e.getMessage();
			errDesc = null;
			errHelpId = "unknown";
		}

		eFrame = new JFrame("Pass Vault Plus: Error");
		eFrame.getContentPane().setLayout(new BorderLayout());

		eFrame.getContentPane().add(buildTopPanel(errTitle, errDesc, errHelpId), BorderLayout.NORTH);

		eFrame.getContentPane().add(buildDetailsPanel(e, warnings), BorderLayout.CENTER);
		
		eFrame.getContentPane().add(buildButtonPanel(canContinue, canGoToSetup, canQuit, optionalAction), BorderLayout.SOUTH);

		eFrame.pack();
		//eFrame.setResizable(false);
		SwingUtil.center(eFrame);
		eFrame.setVisible(true);
		eFrame.toFront();
	}
	
	private JPanel buildTopPanel(String errTitle, String errDesc, String errHelpId) {
			// com.graham.passvaultplus.PvpContextUI.getActiveUI().notifyInfo("check thread 132");
		final JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		final JLabel tileLabel = new JLabel(errTitle);
		{
			tileLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
			final JPanel leftAlignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			leftAlignPanel.add(tileLabel);
			p.add(leftAlignPanel);
		}
		if (errDesc != null) {
			final JTextArea descTe = new JTextArea();
			final Font f1 = tileLabel.getFont().deriveFont(11.0f);
			descTe.setBackground(tileLabel.getBackground());
			descTe.setFont(f1);
			descTe.setText(errDesc);
			descTe.setEditable(false);
			descTe.setBorder(new EmptyBorder(1, 24, 8, 8));
			final JPanel leftAlignPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			leftAlignPanel2.add(descTe);
			p.add(leftAlignPanel2);
		}
		
		if (errHelpId != null) {
			helpLink = new JTextArea();
			final Font f1 = tileLabel.getFont().deriveFont(11.0f);
			helpLink.setBackground(tileLabel.getBackground());
			helpLink.setForeground(Color.BLUE);
			helpLink.setFont(f1);
			helpLink.setText(PVP_HELP_URL_ERR_BASE + errHelpId + ".html");
			helpLink.setEditable(false);
			helpLink.setBorder(new EmptyBorder(1, 24, 8, 8));
			final JPanel leftAlignPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			leftAlignPanel2.add(helpLink);
			
			final JButton copyButton = new JButton(new CopyUrlAction());
			SwingUtil.makeButtonSmall(copyButton);
			copyButton.setFocusable(false);
			leftAlignPanel2.add(copyButton);
			GoToUrlAction.checkAndAdd(leftAlignPanel2, helpLink.getText());
			
			p.add(leftAlignPanel2);
		}
		
		return p;
	}
	
	private JScrollPane buildDetailsPanel(final Exception e, final StringBuilder warnings) {
		detailsText = new JTextArea();
		if (warnings.length() > 0) {
			detailsText.setText(e.getMessage() + "\n\n" + GenUtil.getExceptionStackTrace(e) + "\n\nWarnings:\n" + warnings.toString());
		} else {
			detailsText.setText(e.getMessage() + "\n\n" + GenUtil.getExceptionStackTrace(e));
		}
		detailsText.setEditable(false);
		detailsScroll = new JScrollPane(detailsText);
		detailsScroll.setVisible(false);
		return detailsScroll;
	}
	
	private JPanel buildButtonPanel(final boolean canContinue, final boolean canGoToSetup, final boolean canQuit, final Action optionalAction) {
		final JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.RIGHT));
		showDetails = new JButton(new ShowErrorDetailsAction());
		p.add(showDetails);
		if (canGoToSetup) {
			p.add(new JButton(new SetupAction()));
		}
		if (canQuit) {
			p.add(new JButton(new QuitAction()));
		}
		if (canContinue) {
			p.add(new JButton(new ContinueAction()));
			eFrame.addWindowListener(new ContinueWindowAdapter());
		} else {
			//eFrame.addWindowListener(e ->  System.exit(0) );
			eFrame.addWindowListener(new QuitWindowAdapter());
		}
		if (optionalAction != null) {
			p.add(new JButton(optionalAction));
		}
		return p;
	}
	
	static class QuitAction extends AbstractAction {
		QuitAction() {
			super("Quit");
		}
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	
	class SetupAction extends AbstractAction {
		SetupAction() {
			super("Setup");
		}
		public void actionPerformed(ActionEvent e) {
			new ContinueAction().actionPerformed(null);
			PvpContext.startApp(true, null);
		}
	}
	
	class ContinueAction extends AbstractAction {
			// TODO test this  - sometimes we want to use text "Continue" and sometimes "Close"
		public ContinueAction() {
			super("Continue");
		}
		public void actionPerformed(ActionEvent e) {
			eFrame.setVisible(false);
			detailsText = null;
			badExceptionMessageCount = 0;
		}
	}
	
	class CopyUrlAction extends AbstractAction {
		public CopyUrlAction() {
			super("Copy Link");
		}
		public void actionPerformed(ActionEvent e) {
			final StringSelection ss = new StringSelection(helpLink.getText());
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		}
	}
	
	class ShowErrorDetailsAction extends AbstractAction {
		boolean detailsShowingNow = false;
		public ShowErrorDetailsAction() {
			super("Show Details");
		}
		public void actionPerformed(ActionEvent e) {
			if (detailsShowingNow) {
				detailsShowingNow = false;
				showDetails.setText("Show Details");
				detailsScroll.setVisible(false);
			} else {
				detailsShowingNow = true;
				showDetails.setText("Hide Details");
				detailsScroll.setVisible(true);
			}
			//eFrame.setMaximumSize(new Dimension(400, 300));
			eFrame.pack();
			//	eFrame.setResizable(false);
			SwingUtil.center(eFrame);
		}
	}

	static class QuitWindowAdapter extends WindowAdapter {
		public void windowClosing(java.awt.event.WindowEvent event) {
			System.exit(0);
		}
	}
	
	class ContinueWindowAdapter extends WindowAdapter {
		public void windowClosing(java.awt.event.WindowEvent event) {
			new ContinueAction().actionPerformed(null);
		}
	}
	
}
