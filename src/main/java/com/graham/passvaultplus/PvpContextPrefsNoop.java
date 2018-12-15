/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

public class PvpContextPrefsNoop extends PvpContextPrefs {

  public PvpContextPrefsNoop(final PvpContext contextParam) {
    super(contextParam, new PvpPrefFacadeNoop());
  }

}
