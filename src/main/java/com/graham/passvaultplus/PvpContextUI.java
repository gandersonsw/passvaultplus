/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import com.graham.passvaultplus.view.DiagnosticsManager;
import com.graham.passvaultplus.view.ErrorFrame;

import javax.swing.*;

/**
 * Interface to general UI. This is available at all times, can get active UI with PvpContext.getActiveUI()
 */
public class PvpContextUI {
	private JFrame uiFrame;
	private boolean canQuitOrGotoSetup = true;
	private StringBuilder warnings = new StringBuilder();
	final DiagnosticsManager diagnosticsManager;
	private ErrorFrame eframe;

	public PvpContextUI(DiagnosticsManager dm) {
		diagnosticsManager = dm;
	}

	public void setFrame(final JFrame uiFrameParam) {
		uiFrame = uiFrameParam;
	}

	/**
	 * To be used when a bad exception happens somewhere in the application.
	 * @canContinue If false, force the application to quit
	 */
	public void notifyBadException(final Exception e, boolean canContinue, final PvpException.GeneralErrCode gErrCode) {
		if (eframe == null) {
			eframe = new ErrorFrame();
		}
		boolean canQuit = true;
		boolean canGoToSetup = true;
		if (!canQuitOrGotoSetup) {
			canContinue = true;
			canGoToSetup = false;
			canQuit = false;
		}
		eframe.notify(e, canContinue, canGoToSetup, canQuit, gErrCode, warnings);
	}

	/**
	 * To be used when a bad exception happens somewhere in the application.
	 * @canContinue If false, force the application to quit
	 */
	public void notifyBadException(final Exception e, boolean canContinue, boolean canGoToSetup, final PvpException.GeneralErrCode gErrCode) {
		if (eframe == null) {
			eframe = new ErrorFrame();
		}
		boolean canQuit = true;
		if (!canQuitOrGotoSetup) {
			canContinue = true;
			canGoToSetup = false;
			canQuit = false;
		}
		eframe.notify(e, canContinue, canGoToSetup, canQuit, gErrCode, warnings);
	}

	public void enableQuitFromError(final boolean enabled) {
		canQuitOrGotoSetup = enabled;
	}

	public void notifyWarning(String s) {
		diagnosticsManager.warning(s, null);
		warnings.append(s);
		warnings.append("\n");
	}

	public void notifyWarning(String s, Exception e) {
		diagnosticsManager.warning(s, e);
		warnings.append(s);
		warnings.append("::");
		warnings.append(e.getMessage());
		warnings.append("\n");
	}

	public void notifyInfo(String s) {
		diagnosticsManager.info(s);
	}

	public void showMessageDialog(String title, String message) {
		ImageIcon icn = PvpContext.getIcon("option-pane-info", PvpContext.OPT_ICN_SCALE);
		JOptionPane.showMessageDialog(uiFrame, message, title, JOptionPane.INFORMATION_MESSAGE, icn);
	}

	public void showErrorDialog(String title, String message) {
		ImageIcon icn = PvpContext.getIcon("option-pane-bang", PvpContext.OPT_ICN_SCALE);
		JOptionPane.showMessageDialog(uiFrame, message, title == null ? "Error" : title, JOptionPane.INFORMATION_MESSAGE, icn);
	}

	public boolean showConfirmDialog(String title, String message) {
		ImageIcon icn = PvpContext.getIcon("option-pane-confirm", PvpContext.OPT_ICN_SCALE);
		return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(uiFrame, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, icn);
	}
}
