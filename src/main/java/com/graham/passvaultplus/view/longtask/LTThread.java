/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

public class LTThread extends Thread {
		public static final String THREAD_NAME = "pvplt";
		public final LTRunner rnr;
		public LTThread(LTRunner r) {
				super(r, THREAD_NAME);
				rnr = r;
		}
}