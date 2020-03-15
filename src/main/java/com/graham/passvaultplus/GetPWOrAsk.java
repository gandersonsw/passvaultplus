/* Copyright (C) 2020 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import com.graham.passvaultplus.view.PinDialog;
import com.graham.passvaultplus.view.PwDialog;

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
		if (prefs.isPasswordSaved() && !passwordWasBad) {
			boolean tryingToGetValidPin = true;
			int pinTryCount = 0;
			while (tryingToGetValidPin) {
				final PinDialog pd = new PinDialog();
				final PinDialog.PinAction action = pd.askForPin(pinTryCount, false);
				if (action == PinDialog.PinAction.Configure) {
					throw new UserAskToChangeFileException();
				} else if (action == PinDialog.PinAction.UsePassword) {
					tryingToGetValidPin = false;
				} else {
					String pin = pd.getPin();
					String password = prefs.loadPassword(pin, prefs.getUsePin());
					if (password != null) {
						//tryingToGetValidPin = false;
						prefs.setPin(pin);
						return password;
					} else if (pinTryCount >= prefs.getPinMaxTry()) {
						PvpContextUI.getActiveUI().notifyInfo("PIN disabled after " + pinTryCount + " trys");
						// too many tries - delete the password
						tryingToGetValidPin = false;
						prefs.clearPassword();
						prefs.pinWasReset = true;
					}
				}
				pinTryCount++;
			}
		}

		final PwDialog pd = new PwDialog();
		final PwDialog.PwAction action = pd.askForPw(passwordWasBad, resourseLocation);

		if (action == PwDialog.PwAction.Configure) {
			throw new UserAskToChangeFileException();
		}

		prefs.setPassword(pd.getPw(), false);
		if (prefs.getUseGoogleDrive()) {
			prefs.pinWasReset = true;
		}
		return prefs.getPassword();
	}
}
