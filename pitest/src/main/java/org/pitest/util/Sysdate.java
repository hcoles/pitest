package org.pitest.util;

public enum Sysdate implements SysdateFunction {

  SYSDATE;

  public long getTimeInMilliseconds() {
    return System.currentTimeMillis();
  }

}
