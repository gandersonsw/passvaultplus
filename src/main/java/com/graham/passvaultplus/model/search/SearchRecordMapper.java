/* Copyright (C) 2021 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.passvaultplus.model.search;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.function.Function;

import java.util.Map;

import com.graham.passvaultplus.model.core.PvpRecord;
import com.graham.passvaultplus.model.core.PvpType;

public class SearchRecordMapper implements Function<PvpRecord, SearchRecord> {
  
  final String filterByType;
  final PvpRecord filterByCategory;
  final boolean checkCategory;
  final boolean checkType;
  final boolean checkText;
  final String filterByTextLC;
  final Pattern pattern; // = Pattern.compile(patternString);
  
  public SearchRecordMapper(final String filterByTypeParam, final String filterByTextParam, final PvpRecord filterByCategoryParam, final boolean checkCategoryParam) {
    filterByType = filterByTypeParam;
    checkCategory = checkCategoryParam;
    filterByCategory = filterByCategoryParam;
    checkType = !filterByType.equals(PvpType.FILTER_ALL_TYPES);
		checkText = filterByTextParam.length() > 0;
    filterByTextLC = filterByTextParam.toLowerCase();
    // Todo escape for regex
    pattern = Pattern.compile("\\b(" + filterByTextLC + ")\\b");
  }
  
  @Override
  public SearchRecord	apply(PvpRecord r) {
    int match = 5;
    if (checkType) {
      if (!PvpType.sameType(r.getType(), filterByType)) {
        return null;
      }
    }
    if (checkCategory) {
      if (filterByCategory == null) {
        if (r.getCategory() != null) {
          return null;
        }
      } else if (r.getCategory() == null) {
        return null;
      } else if (!(r.getCategory().getId() == filterByCategory.getId())) {
        return null;
      }
    }
    if (checkText) {
      match = recordContainsText(r);
      if (match == 0) {
        return null;
      }
    }
    if (r.isArchived() && match > 1) {
      match = 1;
    }
    return new SearchRecord(r, match);
  }
  
  public boolean getCheckType() {
    return checkType;
  }
  
  private int recordContainsText(final PvpRecord r) {
    for (final Map.Entry<String,String> e : r.getCustomFields().entrySet()) {
      final String s = e.getValue();
      if (s != null) {
        boolean isSummaryField = e.getKey().equals(r.getType().getToStringCode());
        String lc = s.toLowerCase();
        if (lc.indexOf(filterByTextLC) != -1) {
          if (isSummaryField) {
            Matcher matcher = pattern.matcher(lc);
            return matcher.find() ? 5 : 4;
          } else {
            return 3;
            // TODO 2
          }
          // 0 to 5, 0=no match, 1=archived, 2=text match in notes or long, 3=text match anywhere, 4=text match in summary, 5=exact text match in summary
        }
      }
    }
    return 0;
  }
}