/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.awt.Component;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.graham.passvaultplus.model.core.StringEncrypt;
import com.graham.passvaultplus.view.EulaDialog;
import com.graham.passvaultplus.view.StartupOptionsFrame;
import com.graham.passvaultplus.view.MainFrame;

/**
 * This is the global context for the entire application.
 * This is not a singleton, although in general there should
 * be one instance of this active at any time.
 */
public class PvpContext {
	static final public boolean JAR_BUILD = true;
	static final public String VERSION = "1.2";

	static private PvpContextUI activeUI;

	public final PvpContextData data;
	public final PvpContextPrefs prefs;
	public final PvpContextUI ui;

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
		PvpContext context = new PvpContext();
		activeUI = context.ui;

	//	context.rtFileInterface = new PvpPersistenceInterface(context);
	//	context.rtDataInterface = new PvpDataInterface(context);
		if (pw != null) {
			System.out.println("at 35353");
			context.prefs.setPassword(pw, false);
		}

		if (!context.prefs.isDataFilePresent()) {
			final EulaDialog eula = new EulaDialog();
			eula.showEula();
		}

		if (!alwaysShowStartupOptions && context.prefs.isDataFilePresent()) {
			try {
				context.dataFileSelectedForStartup();
			} catch (UserAskToChangeFileException cfe) {
				new StartupOptionsFrame(context);
			} catch (Exception e) {
				context.ui.notifyBadException(e, false, PvpException.GeneralErrCode.CantOpenMainWindow);
			}
		} else {
			new StartupOptionsFrame(context);
		}
	}

	static public PvpContextUI getActiveUI() {
		return activeUI;
	}

	public PvpContext() {
		data = new PvpContextData(this);
		prefs = new PvpContextPrefs(this);
		ui = new PvpContextUI(this);
	}

	public void dataFileSelectedForStartup() throws UserAskToChangeFileException, PvpException {
		data.getFileInterface().load(data.getDataInterface());
		//ui.initMainFrame();
		ui.mainFrame = new MainFrame(this);
		ui.schedulePinTimerTask();
		if (prefs.pinWasReset) {
			JOptionPane.showMessageDialog(ui.mainFrame, "The PIN was reset. To use a PIN again, go to the setting panel and enter a PIN.");
		}
	}

	private static final Map<String, ImageIcon> cachedIcons = new HashMap<>();

	public static ImageIcon getIcon(final String imageName) {
		if (cachedIcons.containsKey(imageName)) {
			return cachedIcons.get(imageName);
		}
		try {
			BufferedImage img;
			if (JAR_BUILD) {
				// note path starts with "/" - that starts at the root of the jar, instead of the location of the class.
				InputStream imageStream = PvpContext.class.getResourceAsStream("/images/" + imageName + ".png");
				img = ImageIO.read(imageStream);
			} else {
				img = ImageIO.read(new File("src/main/resources/images/" + imageName + ".png"));
			}

			final ImageIcon i = new ImageIcon(img);
			cachedIcons.put(imageName, i);
			return i;
		} catch (Exception e) {
			PvpContext.getActiveUI().notifyWarning("PvpContext.getIcon :: " + imageName, e);
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
			PvpContext.getActiveUI().notifyWarning("WARN118 cant load resource text:" + rname, e);
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

}
