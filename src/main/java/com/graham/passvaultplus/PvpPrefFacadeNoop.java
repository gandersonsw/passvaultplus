/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus;

public class PvpPrefFacadeNoop extends PvpPrefFacade {
  @Override
  public String get(String k, String d) {
    return d;
  }
  @Override
  public void put(String k, String v) {
  }
  @Override
  public boolean getBoolean(String k, boolean d) {
    return d;
  }
  @Override
  public void putBoolean(String k, boolean v) {
  }
  @Override
  public byte[] getByteArray(String k, byte[] d) {
    return d;
  }
  @Override
  public void putByteArray(String k, byte[] v) {
  }
  @Override
  public int getInt(String k, int d) {
    return d;
  }
  @Override
  public void putInt(String k, int v) {
  }
  @Override
  public long getLong(String k, long d) {
    return d;
  }
  @Override
  public void putLong(String k, long v) {
  }
}
