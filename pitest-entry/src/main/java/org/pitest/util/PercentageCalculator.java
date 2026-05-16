package org.pitest.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

  public static BigDecimal getPercentage(long total, long actual, int precision) {
    if (total == 0) {
      return new BigDecimal(100).setScale(precision, RoundingMode.UNNECESSARY);
    }

    if (actual == 0) {
      return BigDecimal.ZERO.setScale(precision, RoundingMode.UNNECESSARY);
    }

    if (total == actual) {
      return new BigDecimal(100).setScale(precision, RoundingMode.UNNECESSARY);
    }

    BigDecimal result = BigDecimal.valueOf(actual)
        .multiply(BigDecimal.valueOf(100))
        .divide(BigDecimal.valueOf(total), precision, RoundingMode.HALF_UP);

    BigDecimal cap = new BigDecimal(100)
        .subtract(BigDecimal.ONE.movePointLeft(precision))
        .setScale(precision, RoundingMode.UNNECESSARY);
    return result.min(cap);
  }
}
