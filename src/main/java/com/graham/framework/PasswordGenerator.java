/* Copyright (C) 2018 Graham Anderson gandersonsw@gmail.com - All Rights Reserved */
package com.graham.framework;

import java.security.SecureRandom;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class PasswordGenerator {

  public enum CharCategory {
    digit(48, 57),
    lower(97, 122),
    upper(65, 90),
    special(33, 126);

    int start;
    int end;

    CharCategory(int startParam, int endParam) {
      start = startParam;
      end = endParam;
    }

    int getCharSetSize() {
      if (this == special) {
        return this.end - this.start - digit.getCharSetSize() - lower.getCharSetSize() - upper.getCharSetSize() + 1;
      } else {
        return this.end - this.start + 1;
      }
    }

    int getAscii(int index) {
      if (this == special) {
        int offset = special.start;
        if (index < digit.start - offset) {
          return index + offset;
        }
        offset += digit.getCharSetSize();
        if (index < upper.start - offset) {
          return index + offset;
        }
        offset += upper.getCharSetSize();
        if (index < lower.start - offset) {
          return index + offset;
        }
        offset += lower.getCharSetSize();
        return index + offset;
      } else {
        return index + this.start;
      }
    }
  }

  public static String makePassword(PwGenParams params) {
    SecureRandom rand = new SecureRandom();

    int pwlen = params.minLen + rand.nextInt(params.maxLen - params.minLen + 1);

    int catLen = params.charCats.size();
    int requiredCount = 0;
    for (int i = 0; i < catLen; i++) {
      if (params.charCats.get(i).mustHave1) {
        requiredCount++;
      }
    }

    int counts[] = new int[catLen];
    if (pwlen - requiredCount <= 0) {
      for (int i = 0; i < catLen; i++) {
        counts[i] = params.charCats.get(i).mustHave1 ? 1 : 0;
      }
    } else {
      double cweights[] = new double[catLen];
      double total = 0.0;

      for (int i = 0; i < catLen; i++) {
        cweights[i] = rand.nextFloat() * rand.nextFloat() * params.charCats.get(i).weight;
        total += cweights[i];
      }

      double wfactor = pwlen / total;
      int countTotal = 0;
      double frac = 0.0; // keep track of fractional amounts and increment once we get over 0.5
      for (int i = 0; i < catLen; i++) {
        cweights[i] = cweights[i] * wfactor;
        counts[i] = (int)(cweights[i]);
        frac += cweights[i] - counts[i];
        if (frac > 0.5) {
          counts[i] ++;
          frac = frac - 1.0;
        }
        if (params.charCats.get(i).mustHave1 && counts[i] == 0) {
          counts[i] = 1;
          frac = frac - 1.0 + cweights[i];
        }
        countTotal += counts[i];
      }

      int reduction = countTotal - pwlen;
      int reductionIndex = rand.nextInt(catLen);
      while (reduction > 0) {
        if (counts[reductionIndex] > 1 || (!params.charCats.get(reductionIndex).mustHave1 && counts[reductionIndex] > 0)) {
          counts[reductionIndex]--;
          reduction--;
        }
        reductionIndex++;
        if (reductionIndex >= catLen) {
          reductionIndex = 0;
        }
      }
    }

    List<Byte> asciiList = new ArrayList<>();
    for (int i = 0; i < catLen; i++) {
      for (int j = 0; j < counts[i]; j++) {
        int ascii = rand.nextInt(params.charCats.get(i).charCat.getCharSetSize());
        asciiList.add((byte)params.charCats.get(i).charCat.getAscii(ascii));
      }
    }

    //System.out.println("pwlen=" + pwlen + ":" + asciiList.size() + "=" + counts[0] + "+" + counts[1] + "+" + counts[2] + "+" + counts[3]);
    byte[] asciiArr = new byte[pwlen];
    for (int i = 0; i < pwlen; i++) {
      int j = asciiList.size() == 0 ? 0 : rand.nextInt(asciiList.size());
      asciiArr[i] = asciiList.remove(j);
    }

    return new String(asciiArr);
  }

  public static class PwGenParams {
    List<CharCatParam> charCats = new ArrayList<>();
    int minLen = 8;
    int maxLen = 12;
    public PwGenParams() {
      for (CharCategory cc : CharCategory.values()) {
        charCats.add(new CharCatParam(cc));
      }
    }
    public void setCharCat(CharCategory cc, int weight, boolean mustHave1) {
      if (weight < 0 || weight > 10) {
        throw new IllegalArgumentException("weight must be between 0 and 10. Got:" + weight);
      }
      for (CharCatParam ccp : charCats) {
        if (cc == ccp.charCat) {
          ccp.weight = weight;
          ccp.mustHave1 = mustHave1;
        }
      }
    }
    public void setLengths(int min, int max) {
      if (min > max) {
        throw new IllegalArgumentException("Max must be >= min. Got Min:" + min + " Max:" + max);
      }
      if (min < 1) {
        throw new IllegalArgumentException("Min must be more than 0. Got Min:" + min);
      }
      if (max > 1000) {
        throw new IllegalArgumentException("Max must be less than 1001. Got Max:" + max);
      }
      minLen = min;
      maxLen = max;
    }
  }

  public static class CharCatParam {
    int weight = 10;
    boolean mustHave1 = false;
    CharCategory charCat;
    public CharCatParam(CharCategory cc) {
      charCat = cc;
      switch (cc) {
        case digit: weight = 4; break;
        case lower: weight = 10; break;
        case upper: weight = 5; break;
        case special: weight = 2; break;
      }
    }
  }
}
