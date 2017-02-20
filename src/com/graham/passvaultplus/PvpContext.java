/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.awt.Component;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpFileInterface;
import com.graham.passvaultplus.model.core.StringEncrypt;
import com.graham.passvaultplus.view.ErrorFrame;
import com.graham.passvaultplus.view.MainFrame;
import com.graham.passvaultplus.view.PinDialog;
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
	static public String VERSION = "1.1";
	
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
	private String pin = ""; // used for encryption of PIN, and UI timeout
	private boolean usePin;
	private int pinTimeout = -1;
	private int isPasswordSavedState = PWS_NOT_KNOWN;
	private int encryptionStrengthBits;
	
	private PvpViewListContext viewListContext = new PvpViewListContext();
	private Component prefsComponent;
	private Component schemaEditComponent;
	private MainFrame mainFrame;
	private JLabel infoLabel;
	private ErrorFrame eframe;
	private StringBuilder warnings = new StringBuilder();
	private byte[] encryptedPassword;

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
		if (getUsePin() && getPinTimeout() > 0) {
			// System.out.println("dataFileSelectedForStartup: " + getPinTimeout());
			final Timer tmr = new Timer();
			tmr.schedule(new PinTimerTask(this), getPinTimeout() * 60 * 1000);
		}
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
		setEncryptionStrengthBits(esb);
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

		if (password == null && isPasswordSavedState == PWS_NOT_KNOWN) {
			Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			usePin = userPrefs.getBoolean("usepin", false);
			encryptedPassword = userPrefs.getByteArray("ecp", null); // ecp = encrypted cipher password
			if (!isBlankEncryptedPassword()) {
				isPasswordSavedState = PWS_SAVED;
				
				boolean tryingToGetValidPin = true;
				int pinWasBadCount = 0;
				while (tryingToGetValidPin) {
					boolean tryToGetPassword = true;
					if (usePin && pin.length() == 0) {
						final PinDialog pd = new PinDialog();
						final PinDialog.PinAction action = pd.askForPin(pinWasBadCount);
						if (action == PinDialog.PinAction.Configure) {
							throw new UserAskToChangeFileException();
						}
						pin = pd.getPin();
						if (action == PinDialog.PinAction.UsePassword) {
							tryToGetPassword = false;
							tryingToGetValidPin = false;
						}
					} else {
						tryingToGetValidPin = false;
					}
					
					if (tryToGetPassword) {
						try {
							//System.out.println("encryptedPassword bytes=" + encryptedPassword.length + "   pin:" + pin);
							password = StringEncrypt.decryptString(encryptedPassword, pin, usePin);
							if (password != null) {
								tryingToGetValidPin = false;
							} else {
								pin = "";
								
								try {
									Thread.sleep(pinWasBadCount * 1000);  // sleep for number of seconds for how may tries this is
								} catch (InterruptedException e) {
									this.notifyWarning("getPasswordOrAskUser:sleep" +  e.getMessage());
								}
								
							}
							//System.out.println("decrypt: password is:" + password);
						} catch (PvpException e) {
							pin = "";
							this.notifyBadException(e, true, null);
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					pinWasBadCount++;
				}
			}  else {
				isPasswordSavedState = PWS_NOT_SAVED;
			}
		}
		
		if (password != null && password.trim().length() > 0 && !passwordWasBad) {
			return password;
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
	
	private boolean isBlankEncryptedPassword() {
		if (encryptedPassword == null) {
			return true;
		}
		if (encryptedPassword.length > 4) {
			return false;
		}
		for (int i = 0; i < encryptedPassword.length; i++) {
			if (encryptedPassword[i] != 0) {
				return false;
			}
		}
		return true;
	}

	public boolean isPasswordSaved() {
		if (isPasswordSavedState == PWS_NOT_KNOWN) {
			getPassword();
		}
		return isPasswordSavedState == PWS_SAVED;
	}

	public String getPassword() {
		return password;
	}
	
	public String getPin() {
		return pin;
	}
	
	public boolean getUsePin() {
		if (isPasswordSavedState == PWS_NOT_KNOWN) {
			final Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			usePin = userPrefs.getBoolean("usepin", false);
		}
		
		return usePin;
	}
	
	public int getPinTimeout() {
		if (pinTimeout == -1) {
			final Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			pinTimeout = userPrefs.getInt("pintineout", 30);
		}
		return pinTimeout;
	}
	
	public void setPinTimeout(final int t) {
		pinTimeout = t;
		Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
		userPrefs.putInt("pintineout", pinTimeout);
	}

	public void setPasswordAndPin(String passwordParam, boolean makePersistant, String pinParam, boolean usePinParam) {
		pin = pinParam;
		if (usePin != usePinParam) {
			usePin = usePinParam;
			Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			userPrefs.putBoolean("usepin", usePin);
		}
		setPassword(passwordParam, makePersistant);
	}
	
	public void setPassword(String passwordParam, boolean makePersistant) {
		password = passwordParam;
		try {
			//System.out.println("encrypt: password is:" + passwordParam + "   pin:" + pin);
			encryptedPassword = StringEncrypt.encryptString(passwordParam, pin, usePin);
			//System.out.println("sp: encryptedPassword bytes=" + encryptedPassword.length);
		} catch (PvpException e) {
			this.notifyBadException(e, true, null);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] passwordToPersist;
		if (makePersistant) {
			passwordToPersist = encryptedPassword;
		} else if (isPasswordSavedState == PWS_SAVED) {
			passwordToPersist = new byte[4];
		} else {
			passwordToPersist = null;
		}
		if (passwordToPersist != null) {
			//System.out.println("saving password" + passwordToPersist.length);
			Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			userPrefs.putByteArray("ecp", passwordToPersist); // ecp = encrypted cipher password
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
		getInfoLabel().setText(getInfoLabelText());
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
	
	/**
	 * To be called when a big change is made, and the database should be saved to the file, and the data view should be refreshed
	 */
	public void saveAndRefreshDataList() {
		getFileInterface().save(getDataInterface());
		getViewListContext().getListTableModel().filterUIChanged();
	}
	
	private String getInfoLabelText() {
		int bits = getEncryptionStrengthBits();
		String encrytpStr;
		if (bits == 0) {
			encrytpStr = "None";
		} else {
			encrytpStr = bits + "bit AES";
		}
		return "v" + VERSION + " © 2017    Encryption:" + encrytpStr;
	}
	
	public JLabel getInfoLabel() {
		if (infoLabel == null) {
			infoLabel = new JLabel(getInfoLabelText());
			final Font f = infoLabel.getFont().deriveFont(infoLabel.getFont().getSize() - 2.0f);
			infoLabel.setFont(f);
		}
		
		return infoLabel;
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
