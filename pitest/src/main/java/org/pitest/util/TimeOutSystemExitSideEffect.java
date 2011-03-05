package org.pitest.util;

import org.pitest.functional.SideEffect;

public enum TimeOutSystemExitSideEffect implements SideEffect {

  INSTANCE;

  public void apply() {
    System.exit(ExitCode.TIMEOUT.getCode());
  }

}
