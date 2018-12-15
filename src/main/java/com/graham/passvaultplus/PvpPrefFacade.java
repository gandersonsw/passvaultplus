/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PvpPrefFacade {
  private final Preferences userPrefs = Preferences.userNodeForPackage(PvpContext.class);

  static public void resetGlobalPrefs() {
    final Preferences up = Preferences.userNodeForPackage(PvpContext.class);
    try {
      up.clear();
    } catch (BackingStoreException bse) {
      bse.printStackTrace();
    }
  }

  public String get(String k, String d) {
    return userPrefs.get(k, d);
  }

  public void put(String k, String v) {
    userPrefs.put(k, v);
  }

  public boolean getBoolean(String k, boolean d) {
    return userPrefs.getBoolean(k, d);
  }

  public void putBoolean(String k, boolean v) {
    userPrefs.putBoolean(k, v);
  }

  public byte[] getByteArray(String k, byte[] d) {
    return userPrefs.getByteArray(k, d);
  }

  public void putByteArray(String k, byte[] v) {
    userPrefs.putByteArray(k, v);
  }

  public int getInt(String k, int d) {
    return userPrefs.getInt(k, d);
  }

  public void putInt(String k, int v) {
    userPrefs.putInt(k, v);
  }

  public long getLong(String k, long d) {
    return userPrefs.getLong(k, d);
  }

  public void putLong(String k, long v) {
    userPrefs.putLong(k, v);
  }

}
