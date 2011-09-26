package org.pitest.util;

import org.pitest.functional.SideEffect;
import org.pitest.mutationtest.execute.Reporter;

public class TimeOutSystemExitSideEffect implements SideEffect {

  private final Reporter r;

  public TimeOutSystemExitSideEffect(final Reporter r) {
    this.r = r;
  }

  public void apply() {
    this.r.done();
    System.exit(ExitCode.TIMEOUT.getCode());
  }

}
