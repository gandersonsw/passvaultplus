/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.io.File;

import com.graham.passvaultplus.model.RecordListViewOptions;
import com.graham.passvaultplus.model.core.StringEncrypt;
import com.graham.passvaultplus.view.longtask.LTManager;

import javax.swing.SwingUtilities;

public class PvpContextPrefs {
	static final int PWS_NOT_KNOWN = 0; // dont know because we havent looked in prefs
	static final int PWS_SAVED = 1;     // the user asked the password to be saved in prefs
	static final int PWS_NOT_SAVED = 2; // the user asked the password to not be saved in prefs

	final PvpPrefFacade userPrefs;

	private String dataFilePath;
  private File dataFile;

	private boolean pinPrefsLoaded = false;
	private String pin = ""; // used for encryption of PIN, and UI timeout
	private boolean usePin;
	private int pinTimeout = -1;
	private int pinMaxTry = -1;
	boolean pinWasReset = false;

  int isPasswordSavedState = PWS_NOT_KNOWN;
  String password; // used for encryption of data file
  private int encryptionStrengthBits;
	byte[] encryptedPassword;

  private boolean userPrefsLoaded = false;
  private boolean showDashboard = true;
  private boolean useGoogleDrive = false;
  private boolean showDiagnostics = false;
  private String googleDriveDocId;
  private long googleDriveDocUpdateDate;

  // TODO this is not persisted yet
	private RecordListViewOptions recordListViewOptions = new RecordListViewOptions();

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
		target.setPassword(source.getPassword(), source.isPasswordSaved());
		target.setUsePin(source.getUsePin());
		target.setPin(source.getPin());
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
		GetPWOrAsk pw = new GetPWOrAsk(passwordWasBad, resourseLocation, this);
		try {
			LTManager.waitingUserInputStart();
			if (SwingUtilities.isEventDispatchThread()) {
				pw.run();
			} else {
				SwingUtilities.invokeAndWait(pw);
			}
		} catch (Exception e) {
			PvpContextUI.getActiveUI().notifyWarning("getPasswordOrAskUser error", e);
		} finally {
			LTManager.waitingUserInputEnd();
		}
		if (pw.resultException != null) {
			throw pw.resultException;
		}
		return pw.resultString;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pinParam) {
		pin = pinParam; // the Pin is not saved in prefs, it is entered by user, or saved in database
	}

	private void loadPinPrefs() {
		if (pinPrefsLoaded) {
			return;
		}
		usePin = userPrefs.getBoolean("usepin", false);
		pinTimeout = userPrefs.getInt("pintimeout", 30);
		pinMaxTry = userPrefs.getInt("pinmaxtry", 5);
		pinPrefsLoaded = true;
	}

	public boolean getUsePin() {
		loadPinPrefs(); // call this to make sure usePin is set correctly
		return usePin;
	}

	public void setUsePin(boolean usePinParam) {
		loadPinPrefs();
		if (usePin != usePinParam) {
			usePin = usePinParam;
			userPrefs.putBoolean("usepin", usePin);
		}
	}

	public int getPinTimeout() {
		loadPinPrefs();
		return pinTimeout;
	}

	public void setPinTimeout(final int t) {
		loadPinPrefs();
		pinTimeout = t;
		userPrefs.putInt("pintimeout", pinTimeout);
	}

	public int getPinMaxTry() {
		loadPinPrefs();
		return pinMaxTry;
	}

	public void setPinMaxTry(final int mt) {
		loadPinPrefs();
		pinMaxTry = mt;
		userPrefs.putInt("pinmaxtry", pinMaxTry);
	}

	public void setPassword(String passwordParam, boolean makePersistant) {
		password = passwordParam;
		try {
			encryptedPassword = StringEncrypt.encryptString(passwordParam, pin, getUsePin());
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

	public RecordListViewOptions getRecordListViewOptions() {
		return this.recordListViewOptions;
	}
}
