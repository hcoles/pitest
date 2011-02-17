package org.pitest.mutationtest.instrument;

import org.pitest.util.AbstractMonitor;
import org.pitest.util.ExitCode;

public class TimeoutWatchDog extends AbstractMonitor {

  private final long timeOut;
  private final long sleepInterval;

  public TimeoutWatchDog(final long maxAllowedTime) {
    this.timeOut = System.currentTimeMillis() + maxAllowedTime;
    this.sleepInterval = (maxAllowedTime / 10) + 1;
  }

  @Override
  protected void process() {
    try {
      Thread.sleep(this.sleepInterval);
    } catch (final InterruptedException e) {
      // swallow
    }
    if ((System.currentTimeMillis() > this.timeOut)
        && !this.shutdownRequested()) {
      System.out.println("Timed out without recovery. Exiting");
      System.exit(ExitCode.TIMEOUT.getCode());
    }
  }

}
