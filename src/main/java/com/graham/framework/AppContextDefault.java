package com.graham.framework;

public class AppContextDefault implements AppContext {
	@Override
	public void notifyInfo(String s) {
		System.out.println("INFO: " + s);
	}

	@Override
	public void notifyWarning(String s, Exception e) {
		System.out.println("WARN: " + s);
		e.printStackTrace();
	}

	@Override
	public void notifyError(Exception e, boolean canContinue) {
		e.printStackTrace();
		if (!canContinue) {
			System.exit(-1);
		}
	}
}
