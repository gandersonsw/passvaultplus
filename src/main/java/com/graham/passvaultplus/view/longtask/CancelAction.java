/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CancelAction extends AbstractAction {
		final private LTRunnerSync runner;

		CancelAction(LTRunnerSync runnerParam) {
				super("Cancel");
				runner = runnerParam;
				this.setEnabled(runner.canCancelNow());
		}

		public void actionPerformed(ActionEvent e) {
				System.out.println("CancelAction.actionPerformed.A");

				runner.cancel();
				if (runner.wasCanceled) {
						runner.killStuff();
				} else {
						System.out.println("CancelAction.actionPerformed.B should not get here because button should be disabled");
					//	runner.setCannotCancel();
				}
		}
}
