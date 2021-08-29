/* Copyright (C) 2021 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.core;

public class PvpRecordDeleted {
    
  	private final int id;
    private final int hash;
    
    public PvpRecordDeleted(int idParam, int hashParam) {
      id = idParam;
      hash = hashParam;
    }
    
    public int getId() {
      return id;
    }
    
    public int getHash() {
      return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj != null && obj instanceof PvpRecordDeleted) {
        PvpRecordDeleted other = (PvpRecordDeleted)obj;
        return id == other.id && hash == other.hash;
      }
      return false;
    }
    
    @Override
    public int hashCode() {
      return id;
    }
}