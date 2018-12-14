/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.awt.Font;
import java.awt.Component;
import java.util.Timer;

import javax.swing.JLabel;

import com.graham.passvaultplus.view.DiagnosticsManager;
import com.graham.passvaultplus.view.ErrorFrame;
import com.graham.passvaultplus.view.MainFrame;

import com.graham.passvaultplus.view.dashboard.DashBoardBuilder;
import com.graham.passvaultplus.view.recordlist.PvpViewListContext;

public class PvpContextUI {
  private final MyUndoManager undoManager;
	private final TabManager tabManager;
  private final PvpContext context;
  private Timer pinTimer;

	private PvpViewListContext viewListContext = new PvpViewListContext();
	private Component prefsComponent;
	private Component schemaEditComponent;
	private Component dashboardComponent;
	MainFrame mainFrame;
	private JLabel infoLabel;
	private ErrorFrame eframe;
	private boolean canQuitOrGotoSetup = true;
	private StringBuilder warnings = new StringBuilder();
  final DiagnosticsManager diagnosticsManager;

  PvpContextUI(PvpContext c) {
    undoManager = new MyUndoManager(c);
  	tabManager = new TabManager(c);
    diagnosticsManager = new DiagnosticsManager(c);
    context = c;
  }

	public Component getPrefsComponent() {
		return prefsComponent;
	}

	public void setPrefsComponent(final Component c) {
		prefsComponent = c;
	}

	public Component getSchemaEditComponent() {
		return schemaEditComponent;
	}

	public void setSchemaEditComponent(final Component c) {
		schemaEditComponent = c;
	}

	public void schedulePinTimerTask() {
		cancelPinTimerTask();
		if (context.prefs.getUsePin() && context.prefs.getPinTimeout() > 0) {
			pinTimer = new Timer();
			pinTimer.schedule(new PinTimerTask(context), context.prefs.getPinTimeout() * 60 * 1000);
		}
	}

	private void cancelPinTimerTask() {
		if (pinTimer != null) {
			pinTimer.cancel();
		}
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

	public PvpViewListContext getViewListContext() {
		return viewListContext;
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	public MyUndoManager getUndoManager() {
		return undoManager;
	}

	public TabManager getTabManager() {
		return tabManager;
	}

  public void refreshInfoLabelText() {
    getInfoLabel().setText(getInfoLabelText());
  }

  public JLabel getInfoLabel() {
    if (infoLabel == null) {
      infoLabel = new JLabel(getInfoLabelText());
      final Font f = infoLabel.getFont().deriveFont(infoLabel.getFont().getSize() - 2.0f);
      infoLabel.setFont(f);
    }

    return infoLabel;
  }

  private String getInfoLabelText() {
    int bits = context.prefs.getEncryptionStrengthBits();
    String encrytpStr;
    if (bits == 0) {
      encrytpStr = "None";
    } else {
      encrytpStr = bits + "bit AES";
    }
    return "v" + PvpContext.VERSION + " © 2017    Encryption:" + encrytpStr;
  }

  public void checkOtherTabs() {
    checkDashboard();
    diagnosticsManager.checkDiagnostics();
  }

  public void checkDashboard() {
    if (context.prefs.getShowDashboard() && dashboardComponent == null) {
      try {
        dashboardComponent = DashBoardBuilder.buildDashBoard(context);
        getTabManager().addOtherTab("Dashboard", dashboardComponent);
      } catch (Exception e) {
        // if the dashboard fails to load, dont crash the app
        e.printStackTrace();
      }
    } else if (!context.prefs.getShowDashboard() && dashboardComponent != null) {
      getTabManager().removeOtherTab(dashboardComponent);
      dashboardComponent = null;
    }
  }
}
