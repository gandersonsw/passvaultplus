package com.graham.passvaultplus;

import com.graham.passvaultplus.model.core.StringEncrypt;
import com.graham.passvaultplus.view.PinDialog;
import com.graham.passvaultplus.view.PwDialog;
import com.graham.util.StringUtil;

public class GetPWOrAsk implements Runnable {
	final boolean passwordWasBad;
	final String resourseLocation;
	final PvpContextPrefs prefs;

	String resultString;
	UserAskToChangeFileException resultException;

	public GetPWOrAsk(final boolean passwordWasBadParam, final String resourseLocationParam, final PvpContextPrefs prefsParam) {
		passwordWasBad = passwordWasBadParam;
		resourseLocation = resourseLocationParam;
		prefs = prefsParam;
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
		if (prefs.getPassword() == null && prefs.isPasswordSavedState == PvpContextPrefs.PWS_NOT_KNOWN) {
			boolean usePin = prefs.getUsePin();
			prefs.encryptedPassword = prefs.userPrefs.getByteArray("ecp", null); // ecp = encrypted cipher password
			if (!prefs.isBlankEncryptedPassword() && prefs.isPasswordSaved()) {
				boolean tryingToGetValidPin = true;
				int pinTryCount = 0;
				while (tryingToGetValidPin) {
					boolean tryToGetPassword = true;
					if (usePin && prefs.getPin().length() == 0) {
						final PinDialog pd = new PinDialog();
						final PinDialog.PinAction action = pd.askForPin(pinTryCount, false);
						if (action == PinDialog.PinAction.Configure) {
							throw new UserAskToChangeFileException();
						}
						//pin = pd.getPin();
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
							prefs.password = StringEncrypt.decryptString(prefs.encryptedPassword, prefs.getPin(), usePin);
							if (prefs.password != null) {
								tryingToGetValidPin = false;
							} else {
								if (pinTryCount >= prefs.getPinMaxTry()) {
									PvpContextUI.getActiveUI().notifyInfo("PIN disabled after " + pinTryCount + " trys");
									// too many tries - delete the password
									tryingToGetValidPin = false;
									prefs.clearPassword();
									prefs.pinWasReset = true;
								}
								prefs.setPin(""); // pin = "";
							}
						} catch (PvpException e) {
							prefs.setPin(""); // pin = "";
							PvpContextUI.getActiveUI().notifyBadException(e, true, null);
						}
					}
				}
			} else if (usePin) {
				prefs.pinWasReset = true;
			}
		}

		if (StringUtil.stringNotEmptyTrim(prefs.getPassword()) && !passwordWasBad) {
			return prefs.getPassword(); //  password;
		}
		final PwDialog pd = new PwDialog();
		final PwDialog.PwAction action = pd.askForPw(passwordWasBad, resourseLocation);

		if (action == PwDialog.PwAction.Configure) {
			throw new UserAskToChangeFileException();
		}

		// TODO ? remove this comment, it does not apply anymore I think. its possible the password was wrong and it was saved. if thats the case, dont save the new entered one here
	//	password = pd.getPw();
		prefs.setPassword(pd.getPw(), false);
		return prefs.getPassword();
	}
}
