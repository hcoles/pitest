package org.pitest.util;

public class PercentageCalculator {
  public static int getPercentage(long total, long actual) {
    if (total == 0) {
      return 100;
    }

    if (actual == 0) {
      return 0;
    }

    if (total == actual) {
      return 100;
    }

    return Math.min(99, Math.round((100f / total) * actual));
  }
}
