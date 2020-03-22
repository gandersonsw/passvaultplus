package com.graham.passvaultplus;

public class PvpContextPrefsOverridePW extends PvpContextPrefsNoop {

	private String passwordOverride;

	public PvpContextPrefsOverridePW(String pw) {
		passwordOverride = pw;
	}

	@Override
	public String getPassword() {
		return passwordOverride;
	}

}
