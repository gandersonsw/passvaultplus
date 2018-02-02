/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.util.TimerTask;

import com.graham.passvaultplus.view.PinDialog;
import com.graham.passvaultplus.view.PinDialog.PinAction;
import com.graham.passvaultplus.view.PwDialog;
import com.graham.passvaultplus.view.PwDialog.PwAction;

public class PinTimerTask extends TimerTask {
	
	final private PvpContext context;
	final private String pinAtCreate;
	
	public PinTimerTask(final PvpContext contextParam) {
		context = contextParam;
		pinAtCreate = context.getPin();
	}

	@Override
	public void run() {
		context.getMainFrame().setVisible(false);
		
		if (!context.isBlankEncryptedPassword() && context.isPasswordSaved()) {
			int tryCount = 0;
			while (true) {
				final PinDialog pd = new PinDialog();
				pd.setShowConfigButton(false);
				PinAction action = pd.askForPin(tryCount);
				tryCount++;
				
				if (action == PinAction.UsePassword) {
					askUserForPassword(false);
					return;
				} else if (action == PinAction.Configure) {
					// should never get here since the Configure button is hidden
					context.notifyWarning("PinTimerTask:PinDialog:Action=Configure");
				} else { // user pressed Okay
					final String pin = pd.getPin();
					if (pin.equals(pinAtCreate)) {
						context.getMainFrame().setVisible(true);
						context.schedulePinTimerTask();
						return;
					} else {
						if (tryCount >= context.getPinMaxTry()) {
							context.notifyInfo("PIN disabled after " + tryCount + " trys");
							// too many tries - delete the password
							context.clearPassword();
							askUserForPassword(true);
							return;
						}
					}
				}
				
			}
		} else {
			askUserForPassword(false);
		}
		
	}
	
	private void askUserForPassword(final boolean pinTryMaxed) {
		boolean wasPasswordBad = false;
		while (true) {
			final PwDialog pd = new PwDialog();
			pd.setShowConfigButton(false);
			PwAction action = pd.askForPw(wasPasswordBad, "");
			wasPasswordBad = true;
			if (action == PwAction.Configure) {
				// should never get here since the Configure button is hidden
				context.notifyWarning("PinTimerTask:PwDialog:Action=Configure");
			} else {
				final String pw = pd.getPw();
				if (pw.equals(context.getPassword())) {
					context.getMainFrame().setVisible(true);
					context.schedulePinTimerTask();
					if (pinTryMaxed) {
						context.unclearPassword();
					}
					return;
				}
			}
			
		}
	}

}
