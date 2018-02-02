/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.awt.Component;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.graham.passvaultplus.model.core.PvpDataInterface;
import com.graham.passvaultplus.model.core.PvpPersistenceInterface;
import com.graham.passvaultplus.model.core.StringEncrypt;
import com.graham.passvaultplus.view.DiagnosticsManager;
import com.graham.passvaultplus.view.ErrorFrame;
import com.graham.passvaultplus.view.EulaDialog;
import com.graham.passvaultplus.view.MainFrame;
import com.graham.passvaultplus.view.PinDialog;
import com.graham.passvaultplus.view.PwDialog;
import com.graham.passvaultplus.view.StartupOptionsFrame;
import com.graham.passvaultplus.view.dashboard.DashBoardBuilder;
import com.graham.passvaultplus.view.recordlist.PvpViewListContext;

/**
 * This is the global context for the entire application.
 * This is not a singleton, although in general there should 
 * be one instance of this active at any time.
 */
public class PvpContext {
	static final public boolean JAR_BUILD = true;
	static final public String VERSION = "1.2";
	
	static private final int PWS_NOT_KNOWN = 0; // dont know because we havent looked in prefs
	static private final int PWS_SAVED = 1;     // the user asked the password to be saved in prefs
	static private final int PWS_NOT_SAVED = 2; // the user asked the password to not be saved in prefs
	
	static private PvpContext activeContext;
	
	private final MyUndoManager undoManager = new MyUndoManager(this);
	private final TabManager tabManager = new TabManager(this);

	private PvpPersistenceInterface rtFileInterface;
	private PvpDataInterface rtDataInterface;
	
	private String dataFilePath;
	private File dataFile;
	private String password; // used for encryption of data file
	
	private String pin = ""; // used for encryption of PIN, and UI timeout
	private boolean usePin;
	private int pinTimeout = -1;
	private int pinMaxTry = -1;
	private Timer pinTimer;
	
	private int isPasswordSavedState = PWS_NOT_KNOWN;
	private int encryptionStrengthBits;
	
	private PvpViewListContext viewListContext = new PvpViewListContext();
	private Component prefsComponent;
	private Component schemaEditComponent;
	private Component dashboardComponent;
	private MainFrame mainFrame;
	private JLabel infoLabel;
	private ErrorFrame eframe;
	private boolean canQuitOrGotoSetup = true;
	private StringBuilder warnings = new StringBuilder();
	private byte[] encryptedPassword;
	private DiagnosticsManager diagnosticsManager = new DiagnosticsManager();
	
	private boolean userPrefsLoaded = false;
	private boolean showDashboard = true;
	private boolean useGoogleDrive = false;
	private boolean showDiagnostics = false;
	private String googleDriveDocId;
	private boolean pinWasReset = false;
	
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
		activeContext = context;

		context.rtFileInterface = new PvpPersistenceInterface(context);
		context.rtDataInterface = new PvpDataInterface(context);
		if (pw != null) {
			System.out.println("at 35353");
			context.setPassword(pw, false);
		}
		
		if (!context.isDataFilePresent()) {
			final EulaDialog eula = new EulaDialog();
			eula.showEula(context);
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
	
	static public PvpContext getActiveContext() {
		return activeContext;
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
		schedulePinTimerTask();
		if (pinWasReset) {
			JOptionPane.showMessageDialog(mainFrame, "The PIN was reset. To use a PIN again, go to the setting panel and enter a PIN.");
		}
	}

	public PvpPersistenceInterface getFileInterface() {
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
	public String getPasswordOrAskUser(final boolean passwordWasBad, final String resourseLocation) throws UserAskToChangeFileException {

		if (password == null && isPasswordSavedState == PWS_NOT_KNOWN) {
			final Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			usePin = getUsePin();
			encryptedPassword = userPrefs.getByteArray("ecp", null); // ecp = encrypted cipher password
			if (!isBlankEncryptedPassword() && isPasswordSaved()) {
				boolean tryingToGetValidPin = true;
				int pinTryCount = 0;
				while (tryingToGetValidPin) {
					boolean tryToGetPassword = true;
					if (usePin && pin.length() == 0) {
						final PinDialog pd = new PinDialog();
						final PinDialog.PinAction action = pd.askForPin(pinTryCount);
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
					
					pinTryCount++;
					
					if (tryToGetPassword) {
						try {
							password = StringEncrypt.decryptString(encryptedPassword, pin, usePin);
							if (password != null) {
								tryingToGetValidPin = false;
							} else {
								if (pinTryCount >= getPinMaxTry()) {
									this.notifyInfo("PIN disabled after " + pinTryCount + " trys");
									// too many tries - delete the password
									tryingToGetValidPin = false;
									clearPassword();
									pinWasReset = true;
								}
								pin = "";
							}
						} catch (PvpException e) {
							pin = "";
							this.notifyBadException(e, true, null);
						}
					}
				}
			} else if (usePin) {
				pinWasReset = true;
			}
		}
		
		if (password != null && password.trim().length() > 0 && !passwordWasBad) {
			return password;
		}
		
		final PwDialog pd = new PwDialog();
		final PwDialog.PwAction action = pd.askForPw(passwordWasBad, resourseLocation);
		
		if (action == PwDialog.PwAction.Configure) {
			throw new UserAskToChangeFileException();
		}
		
		// its possible the password was wrong and it was saved. if thats the case, dont save the new entered one here
		password = pd.getPw();
		return password;
	}
	
	public boolean isBlankEncryptedPassword() {
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
//		if (isPasswordSavedState == PWS_NOT_KNOWN) {
//			getPassword();
//		}
		
		if (isPasswordSavedState == PWS_NOT_KNOWN) {
			final Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			boolean pws = userPrefs.getBoolean("pwsaved", false);
			isPasswordSavedState = pws ? PWS_SAVED : PWS_NOT_SAVED;
			usePin = userPrefs.getBoolean("usepin", false);
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
		isPasswordSaved(); // call this to make sure usePin is set correctly
		return usePin;
	}
	
	public int getPinTimeout() {
		if (pinTimeout == -1) {
			final Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			pinTimeout = userPrefs.getInt("pintimeout", 30);
		}
		return pinTimeout;
	}
	
	public void setPinTimeout(final int t) {
		pinTimeout = t;
		final Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
		userPrefs.putInt("pintimeout", pinTimeout);
	}
	
	public int getPinMaxTry() {
		if (pinMaxTry == -1) {
			final Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
			pinMaxTry = userPrefs.getInt("pinmaxtry", 5);
		}
		return pinMaxTry;
	}
	
	public void setPinMaxTry(final int mt) {
		pinMaxTry = mt;
		final Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
		userPrefs.putInt("pinmaxtry", pinMaxTry);
	}
	
	public void schedulePinTimerTask() {
		cancelPinTimerTask();
		if (getUsePin() && getPinTimeout() > 0) {
			pinTimer = new Timer();
			pinTimer.schedule(new PinTimerTask(this), getPinTimeout() * 60 * 1000);
		}
	}
	
	private void cancelPinTimerTask() {
		if (pinTimer != null) {
			pinTimer.cancel();
		}
	}

	public void setPasswordAndPin(String passwordParam, boolean makePersistant, String pinParam, boolean usePinParam) {
		pin = pinParam;
		if (pinParam.length() == 0) {
			usePinParam = false;
		}
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
			System.out.println("setPassword PIN:" + pin + ":");
			encryptedPassword = StringEncrypt.encryptString(passwordParam, pin, usePin);
		} catch (PvpException e) {
			this.notifyBadException(e, true, null);
		}
		byte[] passwordToPersist;
		if (makePersistant) {
			passwordToPersist = encryptedPassword;
		} else if (isPasswordSavedState == PWS_SAVED) {
			// password is saved now, do this to clear it out
			passwordToPersist = new byte[4];
		} else {
			passwordToPersist = null;
		}
		final Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
		userPrefs.putBoolean("pwsaved", makePersistant);
		if (passwordToPersist != null) {
			userPrefs.putByteArray("ecp", passwordToPersist); // ecp = encrypted cipher password
		}
		if (makePersistant) {
			isPasswordSavedState = PWS_SAVED;
		} else {
			isPasswordSavedState = PWS_NOT_SAVED;
		}
	}
	
	public void clearPassword() {
		final Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
		userPrefs.putByteArray("ecp", new byte[4]); // ecp = encrypted cipher password
	}
	
	public void unclearPassword() {
		if (isPasswordSavedState == PWS_SAVED) {
			setPassword(password, true);
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

	private void loadUserPrefs() {
		if (userPrefsLoaded) {
			return;
		}
		Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
		showDashboard = userPrefs.getBoolean("showDashboard", false);
		useGoogleDrive = userPrefs.getBoolean("useGoogleDrive", false);
		showDiagnostics = userPrefs.getBoolean("showDiagnostics", false);
		googleDriveDocId = userPrefs.get("googleDriveDocId", "");
		userPrefsLoaded = true;
	}
	
	public boolean getShowDashboard() {
		loadUserPrefs();
		return showDashboard;
	}
	
	public void setShowDashboard(final boolean s) {
		showDashboard = s;
		Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
		userPrefs.putBoolean("showDashboard", showDashboard);
		checkDashboard();
	}
	
	public void checkOtherTabs() {
		checkDashboard();
		diagnosticsManager.checkDiagnostics();
	}
	
	private void checkDashboard() {
		if (getShowDashboard() && dashboardComponent == null) {
			try {
				dashboardComponent = DashBoardBuilder.buildDashBoard(this);
				getTabManager().addOtherTab("Dashboard", dashboardComponent);
			} catch (Exception e) {
				// if the dashboard fails to load, dont crash the app
				e.printStackTrace();
			}
		} else if (!getShowDashboard() && dashboardComponent != null) {
			getTabManager().removeOtherTab(dashboardComponent);
			dashboardComponent = null;
		}
	}
	
	public boolean getUseGoogleDrive() {
		loadUserPrefs();
		return useGoogleDrive;
	}
	
	public void setUseGoogleDrive(final boolean s) {
		useGoogleDrive = s;
		Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
		userPrefs.putBoolean("useGoogleDrive", useGoogleDrive);
	}
	
	public String getGoogleDriveDocId() {
		loadUserPrefs();
		return googleDriveDocId;
	}
	
	public void setGoogleDriveDocId(final String id) {
		googleDriveDocId = id;
		Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
		userPrefs.put("googleDriveDocId", googleDriveDocId);
	}
	
	public boolean getShowDiagnostics() {
		loadUserPrefs();
		return showDiagnostics;
	}
	
	public void setShowDiagnostics(final boolean s) {
		showDiagnostics = s;
		Preferences userPrefs = Preferences.userNodeForPackage(this.getClass());
		userPrefs.putBoolean("showDiagnostics", showDiagnostics);
		diagnosticsManager.checkDiagnostics();
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
	
	/**
	 * To be called when a big change is made, and the database should be saved to the file, and the data view should be refreshed
	 */
	public void saveAndRefreshDataList() {
		getFileInterface().save(getDataInterface(), PvpPersistenceInterface.SaveTrigger.major);
		getViewListContext().filterUIChanged();
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

	private static final Map<String, ImageIcon> cachedIcons = new HashMap<>();
	
	public static ImageIcon getIcon(final String imageName) {
		if (cachedIcons.containsKey(imageName)) {
			return cachedIcons.get(imageName);
		}
		try {
			BufferedImage img;
			if (JAR_BUILD) {
				//PvpContext.getActiveContext().notifyInfo("getIcon1:" + "images/" + imageName + ".png");
				// note path starts with "/" - that starts at the root of the jar, instead of the location of the class.
				InputStream imageStream = PvpContext.class.getResourceAsStream("/images/" + imageName + ".png");
				img = ImageIO.read(imageStream);
			} else {
				//PvpContext.getActiveContext().notifyInfo("getIcon:" + new File("src/main/resources/images/" + imageName + ".png").getAbsolutePath());
				img = ImageIO.read(new File("src/main/resources/images/" + imageName + ".png"));
			}

			final ImageIcon i = new ImageIcon(img);
			cachedIcons.put(imageName, i);
			return i;
		} catch (Exception e) {
			PvpContext.getActiveContext().notifyWarning("PvpContext.getIcon :: " + imageName, e);
			return null;
		}
	}

	public String getResourceText(final String rname) {
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
			this.notifyWarning("WARN118 cant load resource text:" + rname, e);
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
