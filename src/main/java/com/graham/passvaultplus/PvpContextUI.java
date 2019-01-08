/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.view.DiagnosticsManager;
import com.graham.passvaultplus.view.ErrorFrame;
import com.graham.passvaultplus.view.LongTaskUI;

import javax.swing.*;
import java.awt.*;

/**
 * Interface to general UI. This is available at all times, can get active UI with PvpContext.getActiveUI()
 */
public class PvpContextUI {
	static private PvpContextUI activeUI;

	private JFrame uiFrame;
	private boolean canQuitOrGotoSetup = true;
	private StringBuilder warnings = new StringBuilder();
	final DiagnosticsManager diagnosticsManager;
	private ErrorFrame eframe;

	public PvpContextUI(DiagnosticsManager dm) {
		diagnosticsManager = dm;
		activeUI = this;
	}

	static public PvpContextUI getActiveUI() {
				return activeUI;
		}

	public void setFrame(final JFrame uiFrameParam) {
		uiFrame = uiFrameParam;
	}

	public JFrame getFrame() {
		if (uiFrame != null && uiFrame.isVisible()) {
			return uiFrame;
		}
		return null;
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
		JOptionPane.showMessageDialog(getFrame(), message, title, JOptionPane.INFORMATION_MESSAGE, icn);
	}

	public void showErrorDialog(String title, String message) {
		ImageIcon icn = PvpContext.getIcon("option-pane-bang", PvpContext.OPT_ICN_SCALE);
		JOptionPane.showMessageDialog(getFrame(), message, title == null ? "Error" : title, JOptionPane.INFORMATION_MESSAGE, icn);
	}

	public boolean showConfirmDialog(String title, String message) {
		ImageIcon icn = PvpContext.getIcon("option-pane-confirm", PvpContext.OPT_ICN_SCALE);
		return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(getFrame(), message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, icn);
	}

	public static JDialog createDialog(String title) {
			JDialog d = new JDialog(activeUI.uiFrame, title, Dialog.ModalityType.APPLICATION_MODAL);
			return d;
	}

	public static void showDialog(JDialog d) {
			d.pack();
			if (activeUI.uiFrame == null) {
					BCUtil.center(d);
			} else {
					d.setLocationRelativeTo(activeUI.uiFrame);
			}
			LongTaskUI.goingToShowUI();
			d.setVisible(true);

	}

	public static void hideDialog(JDialog d) {
			LongTaskUI.goingToHideUI();
			d.setVisible(false);
	}


}
