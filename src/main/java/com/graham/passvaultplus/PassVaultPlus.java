/* Copyright (C) 2017 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import javax.swing.*;

public class PassVaultPlus {
	
	public static void main(String[] args) {
		System.out.println("main at 1: " + SwingUtilities.isEventDispatchThread());
		//	javax.swing.SwingUtilities.invokeLater(() -> PvpContext.startApp(false, null));
		PvpContext.startApp(false, null);
	}

}
