/* Copyright (C) 2019 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view.longtask;

public class LTStep {
		String description;
		long runtime;
		boolean done;

		public LTStep(String d) {
				description = d;
				runtime = System.currentTimeMillis();
		}

		public void setDone() {
				done = true;
				runtime = System.currentTimeMillis() - runtime;
		}

		@Override
		public String toString() {
				if (done) {
						return description + " (" + (runtime / 1000) + "s)";
				} else {
						return description + "...";
				}
		}
}
