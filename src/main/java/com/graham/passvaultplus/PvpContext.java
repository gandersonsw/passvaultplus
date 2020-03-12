/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.util.ArrayList;

import javax.swing.*;

import com.graham.framework.AppContext;
import com.graham.passvaultplus.model.core.PvpBackingStore;
import com.graham.passvaultplus.view.EulaDialog;
import com.graham.passvaultplus.view.StartupOptionsFrame;
import com.graham.passvaultplus.view.MainFrame;
import com.graham.passvaultplus.view.longtask.*;
import com.graham.passvaultplus.view.recordedit.RecordEditContext;
import com.graham.util.ResourceUtil;
import com.graham.util.StringUtil;

/**
 * This is the global context for the entire application.
 * This is not a singleton, although in general there should
 * be one instance of this active at any time.
 */
public class PvpContext implements AppContext, Thread.UncaughtExceptionHandler {
	static final public String VERSION = "1.2";
	static final public int OPT_ICN_SCALE = 35;

	public final PvpContextData data;
	public final PvpContextPrefs prefs;
	public final PvpContextUI ui;
	public PvpContextUIMainFrame uiMain; // this can be null if the Main UI is not initialized yet

	/**
	 * Action A: Select data file: new StartupOptionsFrame(...)
	 *    needPassword        -> show Password Dialog
	 *    fileLoaded          -> new MainFrame(this)
	 *    error               -> show ErrorFrame
	 *    Quit button clicked -> System.exit(0)
	 * Action B: Show Password Dialog: getPasswordOrAskUser()
	 *    Password entered and no errors -> new MainFrame(this)
	 *    Password entered and errors    -> show ErrorFrame
	 *    user clicked cancel            -> Select data file
	 */
	static public void startApp(final boolean alwaysShowStartupOptions, final String pw, final String pin) {
		StartAppCB cb = new StartAppCB();
		try {
			final PvpContext context = new PvpContext();
			cb.setContext(context);
			Thread.setDefaultUncaughtExceptionHandler(context);
			ResourceUtil.setExceptionHandler((e,rname) -> PvpContextUI.getActiveUI().notifyWarning("WARN118 cant load resource:" + rname, e));

			if (pw != null) {
				context.prefs.setPassword(pw, false);
			}
			if (pin != null) {
				context.prefs.setPin(pin);
			}

			if (!context.prefs.isDataFilePresent()) {
				new EulaDialog().showEula();
			}

			if (!alwaysShowStartupOptions && context.prefs.isDataFilePresent()) {
				LTManager.runWithProgress(() -> context.dataFileSelectedForStartup(), "Loading", cb);
			} else {
				StartupOptionsFrame.showAndContinue(context);
			}
		} catch (Exception e) {
			cb.handleException(null, e);
		}
	}

	@Override
	public void notifyInfo(String message) {
		ui.notifyInfo(message);
	}

	@Override
	public void notifyWarning(String message, Exception e) {
		ui.notifyWarning(message, e);
	}

	@Override
	public void notifyError(Exception e, boolean canContinue) {
		ui.notifyBadException(e, canContinue, PvpException.GeneralErrCode.OtherErr);
	}

	static class StartAppCB extends LTCallbackDefaultImpl {
		PvpContext contextCopy;
		void setContext(PvpContext c) {
			contextCopy = c;
		}
		@Override
		public void handleException(LTRunner lt, Exception e) {
			if (e instanceof UserAskToChangeFileException) {
				StartupOptionsFrame.showAndContinue(contextCopy);
			} else {
				PvpContextUI cui = contextCopy == null || contextCopy.ui == null ? new PvpContextUI() : contextCopy.ui;
				cui.notifyBadException(e, false, PvpException.GeneralErrCode.CantOpenMainWindow);
			}
		}
	}

	public PvpContext() {
		prefs = new PvpContextPrefs();
		data = new PvpContextData(this);
		ui = new PvpContextUI();
	}

	public PvpContext(PvpContext mainContext, PvpContextPrefs tempPrefs) {
		prefs = tempPrefs;
		data = null;
		ui = mainContext.ui;
	}
	
	/**
	 * For testing only
	 */
	public PvpContext(PvpContextPrefs prefsParam, PvpContextData dataParam, PvpContextUI uiParam) {
		prefs = prefsParam;
		data = dataParam;
		ui = uiParam;
	}

	public void uncaughtException(Thread t, Throwable e) {
		if (e instanceof Exception) {
			ui.notifyWarning("UncaughtException", (Exception)e);
		} else {
			e.printStackTrace();
		}
	}

	public void dataFileSelectedForStartup() throws Exception {
		data.getFileInterface().load(data.getDataInterface());
		SwingUtilities.invokeLater(() -> {
			uiMain = new PvpContextUIMainFrame(this);
			uiMain.mainFrame = new MainFrame(this);
			ui.setFrame(uiMain.getMainFrame());
			if (prefs.getUsePin()) {
				if (StringUtil.stringEmpty(prefs.getPin())) {
					String p = data.getDataInterface().getMetadata("pin");
					if (StringUtil.stringNotEmpty(p)) {
						prefs.setPin(p);
						prefs.pinWasReset = false;
					}
				} else {
					if (data.getDataInterface().setMetadata("pin", prefs.getPin())) {
						data.getFileInterface().setAllDirty();
					}
				}
			}
			PinTimerTask.update(this);
			if (prefs.pinWasReset) {
				prefs.setUsePin(false);
				ui.showMessageDialog("PIN Reset", "The PIN was reset. To use a PIN again, go to the setting panel and enter a PIN.");
			}
		});
	}

	public void updateUIForPrefsChange() {
		if (uiMain != null) {
			if (SwingUtilities.isEventDispatchThread()) {
				updateUIForPrefsChangeInternal();
			} else {
				try {
					SwingUtilities.invokeAndWait(() -> updateUIForPrefsChangeInternal());
				} catch (Exception e) { }
			}
		}
	}

	private void updateUIForPrefsChangeInternal() {
		if (uiMain.getMainFrame() != null) {
			uiMain.getMainFrame().refreshInfoLabelText(this);
		}
		uiMain.checkOtherTabs();
	}

	public boolean hasUnsavedChanges(boolean checkBackingStores) {
		for (RecordEditContext editor : uiMain.getRecordEditors()) {
			if (editor.hasUnsavedChanged()) {
				return true;
			}
		}
		if (checkBackingStores) {
			for (PvpBackingStore bs : data.getFileInterface().getEnabledBackingStores(true)) {
				if (bs.isDirty()) {
					return true;
				}
			}
		}
		return false;
	}

		// TODO delete this after tested better
	public java.util.List<java.util.List<com.graham.passvaultplus.model.core.PvpDataMerger.DelRec>> mergeDelRecs = new ArrayList<>();
	public void registerMergeDeletes(java.util.List<com.graham.passvaultplus.model.core.PvpDataMerger.DelRec> dr) {
			if (dr == null || dr.size() == 0) {
					return;
			}
			mergeDelRecs.add(dr);
			if (uiMain != null) {
					uiMain.createDelRecTab(dr);
			}
	}

}
