/* Copyright (C) 2021 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.search;

import java.util.Comparator;
import java.util.Collections;

public class DefaultSearchRecordComparator implements Comparator<SearchRecord> {
  
  final private int orderMultiplier;
  
  static public void doSort(SearchResults results, boolean orderAsc) {
    Collections.sort(results.records, new DefaultSearchRecordComparator(orderAsc));
  }
  
  public DefaultSearchRecordComparator(boolean orderAsc) {
    orderMultiplier = orderAsc ? 1 : -1;
  }
  
  @Override
  public int compare(SearchRecord r1, SearchRecord r2) {
    return orderMultiplier * Integer.compare(r1.match, r2.match);
  }
  
}
