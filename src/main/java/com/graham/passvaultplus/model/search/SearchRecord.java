/* Copyright (C) 2021 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.search;

import com.graham.passvaultplus.model.core.PvpRecord;

public class SearchRecord {
  public final PvpRecord record;
  public final int match; // 0 to 5, 0=no match, 1=archived, 2=text match in notes or long, 3=text match anywhere, 4=text match in summary, 5=exact text match in summary
  
  public SearchRecord(PvpRecord r, int m) {
    record = r;
    match = m;
  }
}
