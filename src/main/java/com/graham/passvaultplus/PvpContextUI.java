/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import com.graham.framework.BCUtil;
import com.graham.passvaultplus.view.DiagnosticsManager;
import com.graham.passvaultplus.view.ErrorFrame;
import com.graham.passvaultplus.view.longtask.LTManager;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Interface to general UI. This is available at all times, can get active UI with PvpContext.getActiveUI()
 */
public class PvpContextUI {
	static private PvpContextUI activeUI;

	final private DiagnosticsManager diagnosticsManager;

	private JFrame uiFrame;
	private boolean canQuitOrGotoSetup = true;
	private StringBuilder warnings = new StringBuilder();
	private ErrorFrame eframe;

	public PvpContextUI() {
		diagnosticsManager = DiagnosticsManager.get();
		activeUI = this;
	}

	static public PvpContextUI getActiveUI() {
				return activeUI;
		}

	public void setFrame(final JFrame uiFrameParam) {
		uiFrame = uiFrameParam;
	}

	public JFrame getFrame() {

			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0211");
			// I think it is safe to call from non-evt thread
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
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0230");
		ImageIcon icn = PvpContext.getIcon("option-pane-info", PvpContext.OPT_ICN_SCALE);
		JOptionPane.showMessageDialog(getFrame(), message, title, JOptionPane.INFORMATION_MESSAGE, icn);
	}

	public void showErrorDialog(String title, String message) {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0231");
		ImageIcon icn = PvpContext.getIcon("option-pane-bang", PvpContext.OPT_ICN_SCALE);
		JOptionPane.showMessageDialog(getFrame(), message, title == null ? "Error" : title, JOptionPane.INFORMATION_MESSAGE, icn);
	}

	public boolean showConfirmDialog(String title, String message) {
			com.graham.passvaultplus.PvpContextUI.checkEvtThread("0232");
		ImageIcon icn = PvpContext.getIcon("option-pane-confirm", PvpContext.OPT_ICN_SCALE);
		return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(getFrame(), message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, icn);
	}

	/**
	 * @param title Can be null, default to "Pass Vault Plus"
	 */
	public static JDialog createDialog(String title) {
		com.graham.passvaultplus.PvpContextUI.checkEvtThread("2301");
		return new JDialog(activeUI.uiFrame, title == null ? "Pass Vault Plus" : title, Dialog.ModalityType.APPLICATION_MODAL);
	}

	public static void showDialog(JDialog d) {
		//com.graham.passvaultplus.PvpContextUI.checkEvtThread("2302");
		d.pack();
		if (activeUI.uiFrame == null) {
			BCUtil.center(d);
		} else {
			d.setLocationRelativeTo(activeUI.uiFrame);
		}
		try {
				if (SwingUtilities.isEventDispatchThread()) {
						SwingUtilities.invokeLater(() -> d.toFront());
						LTManager.waitingUserInputStart();
						d.setVisible(true); // this is the line that causes the dialog to Block
				} else {
						SwingUtilities.invokeAndWait(() -> {
								SwingUtilities.invokeLater(() -> d.toFront());
								LTManager.waitingUserInputStart();
								d.setVisible(true); // this is the line that causes the dialog to Block
						});
						activeUI.notifyWarning("PvpContextUI.showDialog SwingUtilities.isEventDispatchThread() = false");
				}
		} catch (InterruptedException | InvocationTargetException e) {
			activeUI.notifyWarning("PvpContextUI.showDialog", e);
		}
	}

	public static void hideDialog(JDialog d) {
		com.graham.passvaultplus.PvpContextUI.checkEvtThread("0236");
		d.setVisible(false);
		LTManager.waitingUserInputEnd();
	}

	public static void checkEvtThread(String id) {
		if (!SwingUtilities.isEventDispatchThread()) {
			com.graham.passvaultplus.PvpContextUI.getActiveUI().notifyWarning("checkEvtThread failed :: " + id);
		}
	}



}
