/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import com.graham.passvaultplus.view.DiagnosticsManager;
import com.graham.passvaultplus.view.ErrorFrame;
import com.graham.passvaultplus.view.longtask.LTManager;
import com.graham.util.ResourceUtil;
import com.graham.util.SwingUtil;

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
			RunMd md = new RunMd(title, message);
			if (SwingUtilities.isEventDispatchThread()) {
					md.run();
			} else {
					try {
							SwingUtilities.invokeAndWait(() -> md.run());
					} catch (InterruptedException | InvocationTargetException e) {
							activeUI.notifyWarning("PvpContextUI.showMessageDialog", e);
					}
			}
	}

	public void showErrorDialog(String title, String message) {
			RunEd ed = new RunEd(title, message);
			if (SwingUtilities.isEventDispatchThread()) {
					ed.run();
			} else {
					try {
							SwingUtilities.invokeAndWait(() -> ed.run());
					} catch (InterruptedException | InvocationTargetException e) {
							activeUI.notifyWarning("PvpContextUI.showErrorDialog", e);
					}
			}
	}

	public boolean showConfirmDialog(String title, String message) {
			RunScd scd = new RunScd(title, message);
			if (SwingUtilities.isEventDispatchThread()) {
					scd.run();
			} else {
					try {
							SwingUtilities.invokeAndWait(() -> scd.run());
					} catch (InterruptedException | InvocationTargetException e) {
							activeUI.notifyWarning("PvpContextUI.showConfirmDialog", e);
					}
			}
			return scd.getResult() == JOptionPane.OK_OPTION;
	}

	class RunMd implements Runnable {
			// Run Message Dialog
			final String title;
			final String message;
			RunMd(String t, String m) {
					title = t;
					message = m;
			}
			@Override
			public void run() {
					ImageIcon icn = ResourceUtil.getIcon("option-pane-info", PvpContext.OPT_ICN_SCALE);
					final Window w = LTManager.waitingUserInputStart();
					JOptionPane.showMessageDialog(w == null ? getFrame() : w, message, title, JOptionPane.INFORMATION_MESSAGE, icn);
					LTManager.waitingUserInputEnd();
			}
	}

		class RunEd implements Runnable {
				// Run Error Dialog
				final String title;
				final String message;
				RunEd(String t, String m) {
						title = t;
						message = m;
				}
				@Override
				public void run() {
						ImageIcon icn = ResourceUtil.getIcon("option-pane-bang", PvpContext.OPT_ICN_SCALE);
						final Window w = LTManager.waitingUserInputStart();
						JOptionPane.showMessageDialog(w == null ? getFrame() : w, message, title == null ? "Error" : title, JOptionPane.INFORMATION_MESSAGE, icn);
						LTManager.waitingUserInputEnd();
				}
		}

	class RunScd implements Runnable {
			// Run Show Confirm Dialod
			final String title;
			final String message;
			volatile int result;
			RunScd(String t, String m) {
					title = t;
					message = m;
			}
			@Override
			public void run() {
					final ImageIcon icn = ResourceUtil.getIcon("option-pane-confirm", PvpContext.OPT_ICN_SCALE);
					final Window w = LTManager.waitingUserInputStart();
					result = JOptionPane.showConfirmDialog(w == null ? getFrame() : w, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, icn);
					LTManager.waitingUserInputEnd();
			}
			public int getResult() {
					return result;
			}
	}

	/**
	 * @param title Can be null, default to "Pass Vault Plus"
	 */
	public static JDialog createDialog(String title) {
		return new JDialog(activeUI.uiFrame, title == null ? "Pass Vault Plus" : title, Dialog.ModalityType.APPLICATION_MODAL);
	}

	public static void showDialog(JDialog d) {
		d.pack();
		if (activeUI.uiFrame == null) {
			SwingUtil.center(d);
		} else {
			d.setLocationRelativeTo(activeUI.uiFrame);
		}
		Runnable r = () -> {
				SwingUtilities.invokeLater(() -> d.toFront());
				LTManager.waitingUserInputStart();
				d.setVisible(true); // this is the line that causes the dialog to Block
		};
		try {
				if (SwingUtilities.isEventDispatchThread()) {
						r.run();
				} else {
						SwingUtilities.invokeAndWait(r);
						//activeUI.notifyWarning("PvpContextUI.showDialog SwingUtilities.isEventDispatchThread() = false");
				}
		} catch (InterruptedException | InvocationTargetException e) {
			activeUI.notifyWarning("PvpContextUI.showDialog", e);
		}
	}

	public static void hideDialog(JDialog d) {
		com.graham.passvaultplus.PvpContextUI.checkEvtThread("0236");
			if (SwingUtilities.isEventDispatchThread()) {
					d.setVisible(false);
					LTManager.waitingUserInputEnd();
			} else {
					SwingUtilities.invokeLater(() -> {
							d.setVisible(false);
							LTManager.waitingUserInputEnd();
					});
			}
	}

	public static void checkEvtThread(String id) {
		if (!SwingUtilities.isEventDispatchThread()) {
			com.graham.passvaultplus.PvpContextUI.getActiveUI().notifyWarning("checkEvtThread failed :: " + id, new Exception());
		}
	}

}
