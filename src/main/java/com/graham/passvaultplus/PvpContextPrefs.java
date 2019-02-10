/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.io.File;

import com.graham.passvaultplus.model.core.StringEncrypt;
import com.graham.passvaultplus.view.PinDialog;
import com.graham.passvaultplus.view.PwDialog;
import com.graham.passvaultplus.view.longtask.LTManager;

import javax.swing.*;

public class PvpContextPrefs {
	static private final int PWS_NOT_KNOWN = 0; // dont know because we havent looked in prefs
	static private final int PWS_SAVED = 1;     // the user asked the password to be saved in prefs
	static private final int PWS_NOT_SAVED = 2; // the user asked the password to not be saved in prefs

	final PvpPrefFacade userPrefs;

	private String dataFilePath;
  private File dataFile;
  private String password; // used for encryption of data file

	private String pin = ""; // used for encryption of PIN, and UI timeout
	private boolean usePin;
	private int pinTimeout = -1;
	private int pinMaxTry = -1;

  private int isPasswordSavedState = PWS_NOT_KNOWN;
  private int encryptionStrengthBits;
	private byte[] encryptedPassword;
  private boolean userPrefsLoaded = false;
  private boolean showDashboard = true;
  private boolean useGoogleDrive = false;
  private boolean showDiagnostics = false;
  private String googleDriveDocId;
  private long googleDriveDocUpdateDate;
  boolean pinWasReset = false;

	public PvpContextPrefs() {
		this(new PvpPrefFacade());
	}

	PvpContextPrefs(PvpPrefFacade u) {
		userPrefs = u;
	}

	public static PvpContextPrefs copyPrefs(PvpContextPrefs source, PvpContextPrefs target, PvpContext context) {
		target.userPrefsLoaded = true;
		target.setDataFilePath(source.getDataFilePath(),source.getEncryptionStrengthBits());
		target.setPinTimeout(source.getPinTimeout());
		target.setPinMaxTry(source.getPinMaxTry());
		target.setPasswordAndPin(source.getPassword(), source.isPasswordSaved(), source.getPin(), source.getUsePin());
		target.setShowDashboard(source.getShowDashboard());
		target.setUseGoogleDrive(source.getUseGoogleDrive());
		target.setGoogleDriveDocId(source.getGoogleDriveDocId());
		target.setGoogleDriveDocUpdateDate(source.getGoogleDriveDocUpdateDate());
		target.setShowDiagnostics(source.getShowDiagnostics());

		if (context != null) {
			context.updateUIForPrefsChange();
		}

		return target;
	}

	public String getDataFilePath() {
		if (dataFilePath == null) {
			dataFilePath = userPrefs.get("data_file", null);
		}
		return dataFilePath;
	}

	public void setDataFilePath(final String path, final int esb) {
		dataFilePath = path;
		dataFile = null;
		setEncryptionStrengthBits(esb);
		userPrefs.put("data_file", path);
	}

	public File getDataFile() {
		if (dataFile == null) {
			dataFile = new File(getDataFilePath());
		}
		return dataFile;
	}

	public boolean isDataFilePresent() {
		if (getDataFilePath() == null) {
			return false;
		}
		return getDataFile().isFile();
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
		if (isPasswordSavedState == PWS_NOT_KNOWN) {
			boolean pws = userPrefs.getBoolean("pwsaved", false);
			isPasswordSavedState = pws ? PWS_SAVED : PWS_NOT_SAVED;
			usePin = userPrefs.getBoolean("usepin", false);
		}
		return isPasswordSavedState == PWS_SAVED;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * Return the password if it was saved, otherwise, ask user for password.
	 * Will only ask once when the application is started.  Will be saved until quit.
	 * @return
	 */
	public String getPasswordOrAskUser(final boolean passwordWasBad, final String resourseLocation) throws UserAskToChangeFileException {
		GetPWOrAsk pw = new GetPWOrAsk(passwordWasBad, resourseLocation);
		try {
			LTManager.waitingUserInputStart(99);
			SwingUtilities.invokeAndWait(pw);
		} catch (Exception e) {
			PvpContextUI.getActiveUI().notifyWarning("getPasswordOrAskUser error", e);
		} finally {
			LTManager.waitingUserInputEnd(99);
		}
		if (pw.resultException != null) {
			throw pw.resultException;
		}
		return pw.resultString;
	}

	class GetPWOrAsk implements Runnable {
		final boolean passwordWasBad;
		final String resourseLocation;

		String resultString;
		UserAskToChangeFileException resultException;

		public GetPWOrAsk(final boolean passwordWasBadParam, final String resourseLocationParam) {
			passwordWasBad = passwordWasBadParam;
			resourseLocation = resourseLocationParam;
		}

		@Override
		public void run() {
			try {
				resultString = getPasswordOrAskUserInternal();
			} catch (UserAskToChangeFileException e) {
				resultException = e;
			}
		}

	private String getPasswordOrAskUserInternal() throws UserAskToChangeFileException {
		if (password == null && isPasswordSavedState == PWS_NOT_KNOWN) {
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
									PvpContextUI.getActiveUI().notifyInfo("PIN disabled after " + pinTryCount + " trys");
									// too many tries - delete the password
									tryingToGetValidPin = false;
									clearPassword();
									pinWasReset = true;
								}
								pin = "";
							}
						} catch (PvpException e) {
							pin = "";
							PvpContextUI.getActiveUI().notifyBadException(e, true, null);
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
			pinTimeout = userPrefs.getInt("pintimeout", 30);
		}
		return pinTimeout;
	}

	public void setPinTimeout(final int t) {
		pinTimeout = t;
		userPrefs.putInt("pintimeout", pinTimeout);
	}

	public int getPinMaxTry() {
		if (pinMaxTry == -1) {
			pinMaxTry = userPrefs.getInt("pinmaxtry", 5);
		}
		return pinMaxTry;
	}

	public void setPinMaxTry(final int mt) {
		pinMaxTry = mt;
		userPrefs.putInt("pinmaxtry", pinMaxTry);
	}

	public void setPasswordAndPin(String passwordParam, boolean makePersistant, String pinParam, boolean usePinParam) {
		pin = pinParam;
		if (pinParam.length() == 0) {
			usePinParam = false;
		}
		if (usePin != usePinParam) {
			usePin = usePinParam;
			userPrefs.putBoolean("usepin", usePin);
		}
		setPassword(passwordParam, makePersistant);
	}

	public void setPassword(String passwordParam, boolean makePersistant) {
		password = passwordParam;
		try {
			encryptedPassword = StringEncrypt.encryptString(passwordParam, pin, usePin);
		} catch (PvpException e) {
			PvpContextUI.getActiveUI().notifyBadException(e, true, null);
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
		// this is not saved in prefs because it is saved in the file header
		encryptionStrengthBits = esb;
	}

	private void loadUserPrefs() {
		if (userPrefsLoaded) {
			return;
		}
		showDashboard = userPrefs.getBoolean("showDashboard", false);
		useGoogleDrive = userPrefs.getBoolean("useGoogleDrive", false);
		showDiagnostics = userPrefs.getBoolean("showDiagnostics", false);
		googleDriveDocId = userPrefs.get("googleDriveDocId", "");
		googleDriveDocUpdateDate = userPrefs.getLong("googleDriveDocUpdateDate", 0);
		userPrefsLoaded = true;
	}

	public boolean getShowDashboard() {
		loadUserPrefs();
		return showDashboard;
	}

	public void setShowDashboard(final boolean s) {
		showDashboard = s;
		userPrefs.putBoolean("showDashboard", showDashboard);
	}

	public boolean getUseGoogleDrive() {
		loadUserPrefs();
		return useGoogleDrive;
	}

	public void setUseGoogleDrive(final boolean s) {
		useGoogleDrive = s;
		userPrefs.putBoolean("useGoogleDrive", useGoogleDrive);
	}

	public String getGoogleDriveDocId() {
		loadUserPrefs();
		return googleDriveDocId;
	}

	public void setGoogleDriveDocId(final String id) {
		googleDriveDocId = id;
		userPrefs.put("googleDriveDocId", googleDriveDocId);
	}

	public long getGoogleDriveDocUpdateDate() {
		loadUserPrefs();
		return googleDriveDocUpdateDate;
	}

	public void setGoogleDriveDocUpdateDate(final long d) {
		googleDriveDocUpdateDate = d;
		userPrefs.putLong("googleDriveDocUpdateDate", googleDriveDocUpdateDate);
	}

	public boolean getShowDiagnostics() {
		loadUserPrefs();
		return showDiagnostics;
	}

	public void setShowDiagnostics(final boolean s) {
		showDiagnostics = s;
		userPrefs.putBoolean("showDiagnostics", showDiagnostics);
	}
}
