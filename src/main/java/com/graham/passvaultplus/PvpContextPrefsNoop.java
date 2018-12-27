/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

public class PvpContextPrefsNoop extends PvpContextPrefs {

  public PvpContextPrefsNoop(final PvpContext contextParam) {
    super(contextParam, new PvpPrefFacadeNoop());
  }

  /**
   * Return the password if it was saved, otherwise, ask user for password.
   * Will only ask once when the application is started.  Will be saved until quit.
   * @return
   */
  public String getPasswordOrAskUser(final boolean passwordWasBad, final String resourseLocation) throws UserAskToChangeFileException {
    if (passwordWasBad) {
      throw new UserAskToChangeFileException();
    } else {
      return this.getPassword();
    }
  }

}
