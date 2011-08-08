package org.pitest.mutationtest.execute;

import java.util.logging.Logger;

import org.pitest.functional.SideEffect;
import org.pitest.util.AbstractMonitor;
import org.pitest.util.Log;

public class TimeoutWatchDog extends AbstractMonitor {

  private final static Logger LOG = Log.getLogger();

  private final long          startTime;
  private final long          dieAt;
  private final long          sleepInterval;
  private final SideEffect    exitStrategy;
  private final String        testName;

  public TimeoutWatchDog(final SideEffect exitStrategy, final long dieAt,
      final String name) {
    this.startTime = System.currentTimeMillis();
    this.dieAt = dieAt;
    this.sleepInterval = (allowedTime() / 5) + 10;
    this.exitStrategy = exitStrategy;
    this.testName = name;

  }

  @Override
  protected void process() {
    try {
      Thread.sleep(this.sleepInterval);
    } catch (final InterruptedException e) {
      // LOG.fine("Sleeping watchdog woken");
    }
    if ((System.currentTimeMillis() > this.dieAt) && !this.shutdownRequested()) {
      LOG.info("Hard time out after "
          + (System.currentTimeMillis() - this.startTime)
          + "ms. While running " + this.testName + ". Allowed time was "
          + allowedTime() + " Exiting.");
      this.exitStrategy.apply();
      this.requestStop();
    }
  }

  private long allowedTime() {
    return (this.dieAt - this.startTime);
  }

}
