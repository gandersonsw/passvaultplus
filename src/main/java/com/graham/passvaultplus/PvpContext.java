/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.awt.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.graham.passvaultplus.view.EulaDialog;
import com.graham.passvaultplus.view.StartupOptionsFrame;
import com.graham.passvaultplus.view.MainFrame;
import com.graham.passvaultplus.view.longtask.*;

/**
 * This is the global context for the entire application.
 * This is not a singleton, although in general there should
 * be one instance of this active at any time.
 */
public class PvpContext implements Thread.UncaughtExceptionHandler {
	static final public boolean JAR_BUILD = true;
	static final public String VERSION = "1.2";
	static final public int OPT_ICN_SCALE = 35;
	//static final public String USR_CANCELED = "operation canceled by user";

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
	static public void startApp(final boolean alwaysShowStartupOptions, final String pw) {
		StartAppCB cb = new StartAppCB();
		try {
			final PvpContext context = new PvpContext();
			cb.setContext(context);
			Thread.setDefaultUncaughtExceptionHandler(context);

			if (pw != null) {
				context.prefs.setPassword(pw, false);
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

	public void uncaughtException(Thread t, Throwable e) {
		if (e instanceof Exception) {
			ui.notifyWarning("UncaughtException", (Exception)e);
		} else {
			e.printStackTrace();
		}
	}

	public void dataFileSelectedForStartup() throws Exception {
		//LTManager.setTitle("Loading...");
	//		Thread.sleep(3000);
		data.getFileInterface().load(data.getDataInterface());
		SwingUtilities.invokeLater(() -> {
			uiMain = new PvpContextUIMainFrame(this);
			uiMain.mainFrame = new MainFrame(this);
			ui.setFrame(uiMain.getMainFrame());

			//	ui.schedulePinTimerTask();
			if (prefs.pinWasReset) {
				ui.showMessageDialog("PIN Reset", "The PIN was reset. To use a PIN again, go to the setting panel and enter a PIN.");
			}
		});
	}

	private static final Map<String, ImageIcon> cachedIcons = new HashMap<>();

	public static ImageIcon getIcon(final String imageName) {
		return getIcon(imageName, 100);
	}

	public static ImageIcon getIcon(final String imageName, int scalePercent) {
		String cachedName = imageName + scalePercent;
		if (cachedIcons.containsKey(cachedName)) {
			return cachedIcons.get(cachedName);
		}
		try {
			Image img;
			if (JAR_BUILD) {
				// note path starts with "/" - that starts at the root of the jar, instead of the location of the class.
				InputStream imageStream = PvpContext.class.getResourceAsStream("/images/" + imageName + ".png");
				img = ImageIO.read(imageStream);
			} else {
				img = ImageIO.read(new File("src/main/resources/images/" + imageName + ".png"));
			}

			if (scalePercent != 100) {
					double r = scalePercent / 100.0;
					img = img.getScaledInstance((int)(r * img.getWidth(null)), (int)(r * img.getHeight(null)), Image.SCALE_SMOOTH);
			}
			final ImageIcon i = new ImageIcon(img);
			cachedIcons.put(cachedName, i);
			return i;
		} catch (Exception e) {
			PvpContextUI.getActiveUI().notifyWarning("PvpContext.getIcon :: " + imageName, e);
			return null;
		}
	}

	public static String getResourceText(final String rname) {
		InputStream sourceStream = null;
		InputStreamReader isr = null;
		BufferedReader bufR = null;
		try {
			if (PvpContext.JAR_BUILD) {
				// note path starts with "/" - that starts at the root of the jar,
				// instead of the location of the class.
				sourceStream = PvpContext.class.getResourceAsStream("/" + rname + ".txt");
				isr = new InputStreamReader(sourceStream);
			} else {
				File sourceFile = new File("src/main/resources/" + rname + ".txt");
				isr = new FileReader(sourceFile);
			}

			bufR = new BufferedReader(isr);

			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = bufR.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			return sb.toString();

		} catch (Exception e) {
			PvpContextUI.getActiveUI().notifyWarning("WARN118 cant load resource text:" + rname, e);
			return "";
		} finally {
			if (bufR != null) {
				try { bufR.close(); } catch (Exception e) { }
			}
			if (isr != null) {
				try { isr.close(); } catch (Exception e) { }
			}
			if (sourceStream != null) {
				try { sourceStream.close(); } catch (Exception e) { }
			}
		}
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
			if (uiMain.getMainFrame() != null) { // TODO is there a way to get rid of these checks?
					uiMain.getMainFrame().refreshInfoLabelText(this);
			}
			uiMain.checkOtherTabs();
	}

}
