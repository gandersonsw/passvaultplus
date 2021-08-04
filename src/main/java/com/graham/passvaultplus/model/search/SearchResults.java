/* Copyright (C) 2021 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.search;

import java.util.ArrayList;
import java.util.List;

import com.graham.passvaultplus.model.core.PvpRecord;

public class SearchResults {
  public final List<SearchRecord> records;
  public final boolean allTheSameTypeFlag;
  public final boolean allTheSameMatchFlag;
  
  public SearchResults(List<SearchRecord> r, boolean checkType) {
    records = r;
    
    boolean b = true;
    for (int i = 1; i < records.size(); i++) {
      if (records.get(i - 1).match != records.get(i).match) {
        b = false;
        break;
      }
    }
    allTheSameMatchFlag = b;
    
    b = true;
    if (!checkType) {
      for (int i = 1; i < records.size(); i++) {
        if (!records.get(i - 1).record.getType().getName().equals(records.get(i).record.getType().getName())) {
          b = false;
          break;
        }
      }
    }
    allTheSameTypeFlag = b;
  }
  
  public List<PvpRecord> getNestedRecords() {
    List<PvpRecord> rList = new ArrayList<>(records.size());
    for (SearchRecord sr : records) {
      rList.add(sr.record);
    }
    return rList;
  }
}
