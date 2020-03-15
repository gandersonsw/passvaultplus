/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

import com.graham.passvaultplus.view.PinDialog;
import com.graham.passvaultplus.view.PinDialog.PinAction;
import com.graham.passvaultplus.view.PwDialog;
import com.graham.passvaultplus.view.PwDialog.PwAction;

import javax.swing.*;

class PtAel implements AWTEventListener {
	long lastEventTime;
	@Override
	public void eventDispatched(AWTEvent event) {
		if (event instanceof MouseEvent) {
			if (((MouseEvent) event).getClickCount() == 0) {
				return;
			}
		}
		lastEventTime = System.currentTimeMillis();
	}
}

public class PinTimerTask extends TimerTask {

	final private PvpContext context;
	final private String pinAtCreate;
	private Long timeoutTime;
	static private Timer pinTimer;
	static private PtAel evtListener;

	public static void update(final PvpContext c) {
		//System.out.println(">>>>>> PinTimerTask.update.A :: " + c.prefs.getUsePin() + " :: " + c.prefs.getPinTimeout());
		if (pinTimer != null) {
			pinTimer.cancel();
		}
		if (c.prefs.getUsePin() && c.prefs.getPinTimeout() > 0) {
			if (evtListener == null) {
				evtListener = new PtAel();
				Toolkit.getDefaultToolkit().addAWTEventListener(evtListener, AWTEvent.KEY_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK);
			}
			pinTimer = new Timer();
			pinTimer.schedule(new PinTimerTask(c), 2000, 2000);
		}
	}

	private PinTimerTask(final PvpContext contextParam) {
		context = contextParam;
		pinAtCreate = context.prefs.getPin();
		setTimeoutTime(0);
	}

	private void setTimeoutTime(long evtOffset) {
		timeoutTime = System.currentTimeMillis() + (context.prefs.getPinTimeout() * 60 * 1000) - evtOffset;
	}

	@Override
	public void run() {
		if (System.currentTimeMillis() < timeoutTime) {
			return; // not timeout out yet
		}
		long timeLeftFromInput = context.prefs.getPinTimeout() * 60 * 1000 - (System.currentTimeMillis() - evtListener.lastEventTime);
		if (timeLeftFromInput > 2000){
				setTimeoutTime(System.currentTimeMillis() - evtListener.lastEventTime);
				return;
		}

		pinTimer.cancel();
		pinTimer = null;
		SwingUtilities.invokeLater(() -> doUI());
	}

	private void doUI() {
		if (!context.uiMain.getMainFrame().isVisible()) {
			context.ui.notifyInfo("PinTimerTask not executed because MainFrame is already hidden");
			return;
		}
		context.uiMain.getMainFrame().setVisible(false);

		if (context.prefs.getUsePin()) {
			int tryCount = 0;
			while (true) {
				final PinDialog pd = new PinDialog();
				pd.setShowConfigButton(false);
				PinAction action = pd.askForPin(tryCount, context.hasUnsavedChanges(true));
				tryCount++;

				if (action == PinAction.UsePassword) {
					askUserForPassword(false);
					return;
				} else if (action == PinAction.Configure) {
					// should never get here since the Configure button is hidden
					context.ui.notifyWarning("PinTimerTask:PinDialog:Action=Configure");
				} else { // user pressed Okay
					final String pin = pd.getPin();
					if (pin.equals(pinAtCreate)) {
						context.uiMain.getMainFrame().setVisible(true);
						PinTimerTask.update(context);
						return;
					} else {
						if (tryCount >= context.prefs.getPinMaxTry()) {
							context.ui.notifyInfo("PIN disabled after " + tryCount + " trys");
							// too many tries - delete the password
							context.prefs.clearPassword();
							askUserForPassword(true);
							return;
						}
					}
				}
			}
		} else {
			context.ui.notifyWarning("PinTimerTask:PinDialog:UsePin was false");
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
				context.ui.notifyWarning("PinTimerTask:PwDialog:Action=Configure");
			} else {
				final String pw = pd.getPw();
				if (pw.equals(context.prefs.getPassword())) {
					context.uiMain.getMainFrame().setVisible(true);
					PinTimerTask.update(context);
					if (pinTryMaxed) {
						context.prefs.unclearPassword();
					}
					return;
				}
			}
		}
	}

}
