/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpFileInterface;
import com.graham.passvaultplus.view.ErrorFrame;
import com.graham.passvaultplus.view.MainFrame;
import com.graham.passvaultplus.view.PwDialog;
import com.graham.passvaultplus.view.StartupOptionsFrame;
import com.graham.passvaultplus.view.recordlist.PvpViewListContext;

/**
 * This is the global context for the entire application.
 * This is not a singleton, although in general there should 
 * be one instance of this active at any time.
 */
public class PvpContext {
	static final public boolean JAR_BUILD = false;
	
	static private final int PWS_NOT_KNOWN = 0; // dont know because we havent looked in prefs
	static private final int PWS_SAVED = 1;     // the user asked the password to be saved in prefs
	static private final int PWS_NOT_SAVED = 2; // the user asked the password to not be saved in prefs
	
	private final MyUndoManager undoManager = new MyUndoManager(this);
	private final TabManager tabManager = new TabManager(this);

	private PvpFileInterface rtFileInterface;
	private PvpDataInterface rtDataInterface;
	
	private String dataFilePath;
	private File dataFile;
	private String password; // used for encryption of data file
	private int isPasswordSavedState = PWS_NOT_KNOWN;
	private int encryptionStrengthBits;
	
	private PvpViewListContext viewListContext = new PvpViewListContext();
	private Component prefsComponent;
	private Component schemaEditComponent;
	private MainFrame mainFrame;
	private ErrorFrame eframe;
	private StringBuilder warnings = new StringBuilder();

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

		context.rtFileInterface = new PvpFileInterface(context);
		context.rtDataInterface = new PvpDataInterface(context);
		if (pw != null) {
			context.setPassword(pw, false);
		}
		
		if (!alwaysShowStartupOptions && context.isDataFilePresent()) {
			try {
				context.dataFileSelectedForStartup();
			} catch (UserAskToChangeFileException cfe) {
				new StartupOptionsFrame(context);
			} catch (Exception e) {
				context.notifyBadException(e, false, PvpException.GeneralErrCode.CantOpenMainWindow);
			}
		} else {
			new StartupOptionsFrame(context);
		}
	}

	private boolean isDataFilePresent() {
		if (getDataFilePath() == null) {
			return false;
		}
		File f = new File(getDataFilePath());
		return f.isFile();
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

	public void dataFileSelectedForStartup() throws UserAskToChangeFileException, PvpException {
		getFileInterface().load(getDataInterface());
		mainFrame = new MainFrame(this);
	}

	public PvpFileInterface getFileInterface() {
		return this.rtFileInterface;
	}

	public PvpDataInterface getDataInterface() {
		return this.rtDataInterface;
	}

	public String getDataFilePath() {
		if (dataFilePath == null) {
			Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			dataFilePath = userPrefs.get("data_file", null);
		}
		return dataFilePath;
	}

	public void setDataFilePath(final String path, final int esb) {
		dataFilePath = path;
		dataFile = null;
		encryptionStrengthBits = esb;
		Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
		userPrefs.put("data_file", path);
	}
	
	public File getDataFile() {
		if (dataFile == null) {
			dataFile = new File(dataFilePath);
		}
		return dataFile;
	}

	/**
	 * Return the password if it was saved, otherwise, ask user for password.
	 * Will only ask once when the application is started.  Will be saved until quit.
	 * @return
	 */
	public String getPasswordOrAskUser(boolean passwordWasBad) throws UserAskToChangeFileException {
		final String pw = getPassword();
		if (pw != null && pw.trim().length() > 0 && !passwordWasBad) {
			return pw;
		}
		
		final PwDialog pd = new PwDialog();
		final PwDialog.PwAction action = pd.askForPw(passwordWasBad, dataFilePath);
		
		if (action == PwDialog.PwAction.Configure) {
			throw new UserAskToChangeFileException();
		}
		
		// its possible the password was wrong and it was saved. if thats the case, dont save the new entered one here
		password = pd.getPw();
		return password;
	}

	public boolean isPasswordSaved() {
		if (isPasswordSavedState == PWS_NOT_KNOWN) {
			getPassword();
		}
		return isPasswordSavedState == PWS_SAVED;
	}

	public String getPassword() {
		if (password == null && isPasswordSavedState == PWS_NOT_KNOWN) {
			Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			password = userPrefs.get("cp", null); // cipher pw
			if (password != null && password.trim().length() > 0) {
				isPasswordSavedState = PWS_SAVED;
			} else {
				isPasswordSavedState = PWS_NOT_SAVED;
			}
		}
		return password;
	}

	public void setPassword(String passwordParam, boolean makePersistant) {
		password = passwordParam;
		final String passwordToPersit;
		if (makePersistant) {
			passwordToPersit = password;
		} else if (isPasswordSavedState == PWS_SAVED) {
			passwordToPersit = "";
		} else {
			passwordToPersit = null;
		}
		if (passwordToPersit != null) {
			Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			userPrefs.put("cp", passwordToPersit); // cipher pw
		}
		if (makePersistant) {
			isPasswordSavedState = PWS_SAVED;
		} else {
			isPasswordSavedState = PWS_NOT_SAVED;
		}
	}
	
	public int getEncryptionStrengthBits() {
		return encryptionStrengthBits;
	}
	
	public void setEncryptionStrengthBits(final int esb) {
		// this is not saved because it is saved in the file header
		encryptionStrengthBits = esb;
	}

	/**
	 * To be used when a bad exception happens somewhere in the application.
	 * @canContinue If false, force the application to quit
	 */
	public void notifyBadException(final Exception e, final boolean canContinue, final PvpException.GeneralErrCode gErrCode) {
		if (eframe == null) {
			eframe = new ErrorFrame();
		}
		eframe.notify(e, canContinue, gErrCode, warnings);
	}

	public void notifyWarning(String s) {
		warnings.append(s);
		warnings.append("\n");
		// TODO
		System.out.println(s);
	}
	
	public void notifyWarning(String s, Exception e) {
		warnings.append(s);
		warnings.append("::");
		warnings.append(e.getMessage());
		warnings.append("\n");
		// TODO
		System.out.println(s);
		e.printStackTrace();
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

	public static ImageIcon getIcon(final String imageName) {
		try {
			BufferedImage img;
			if (JAR_BUILD) {
				//System.out.println("getIcon1:" + "datafiles/images/" + imageName + ".png");
				// note path starts with "/" - that starts at the root of the jar, instead of the location of the class.
				InputStream imageStream = PvpContext.class.getResourceAsStream("/datafiles/images/" + imageName + ".png");
				img = ImageIO.read(imageStream);
			} else {
				//System.out.println("getIcon:" + new File("datafiles/images/" + imageName + ".png").getAbsolutePath());
				img = ImageIO.read(new File("datafiles/images/" + imageName + ".png"));
			}

			return new ImageIcon(img);
		} catch (IOException e) {
			e.printStackTrace(); // TODO
			return null;
		}
	}

}
