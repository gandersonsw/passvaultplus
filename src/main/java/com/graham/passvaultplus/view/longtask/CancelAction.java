/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CancelAction extends AbstractAction {
		private LTRunnerSync runner;

		CancelAction(LTRunnerSync runnerParam) {
				super("Cancel");
				runner = runnerParam;
		}

		public void actionPerformed(ActionEvent e) {
				System.out.println("CancelAction.actionPerformed.A");
				runner.killStuff();
				runner.setWasCanceled();
		}
}
