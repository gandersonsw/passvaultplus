/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.view;

import com.graham.passvaultplus.view.dashboard.DashBoardBuilder;
import com.graham.passvaultplus.view.prefs.PreferencesBuilder;
import com.graham.passvaultplus.view.recordlist.ViewListBuilder;
import com.graham.passvaultplus.view.schemaedit.SchemaEditBuilder;

public enum OtherTab {
	RecordList(new ViewListBuilder()),
	Prefs(new PreferencesBuilder()),
	SchemaEdit(new SchemaEditBuilder()),
	Dashboard(new DashBoardBuilder()),
	Diagnostics(DiagnosticsManager.get(), false),
	Help(new HelpBuilder());

	OtherTabBuilder builder;
	boolean selectedWhenShown = true;

	OtherTab(OtherTabBuilder b) {
				builder = b;
		}

	OtherTab(OtherTabBuilder b, boolean sws) {
		builder = b;
		selectedWhenShown = sws;
	}

	public OtherTabBuilder getBuilder() {
				return builder;
		}

	public boolean isSelectedWhenShown() {
				return selectedWhenShown;
		}
}
