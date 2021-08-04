/* Copyright (C) 2021 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.search;

import java.util.Comparator;

import com.graham.passvaultplus.model.core.PvpField;
import com.graham.passvaultplus.model.core.PvpRecordComparator;

public class SearchRecordComparator implements Comparator<SearchRecord> {
  
  private PvpRecordComparator cmp;
  
  public SearchRecordComparator(PvpField fieldParam, boolean ascendingParam) {
    cmp = new PvpRecordComparator(fieldParam, ascendingParam);
	}
  
  @Override
  public int compare(SearchRecord r1, SearchRecord r2) {
    return cmp.compare(r1.record, r2.record);
  }
    
}
