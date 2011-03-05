package org.pitest.mutationtest.instrument;

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

  public TimeoutWatchDog(final SideEffect exitStrategy, final long dieAt) {
    this.startTime = System.currentTimeMillis();
    this.dieAt = dieAt;
    this.sleepInterval = (allowedTime() / 5) + 10;
    this.exitStrategy = exitStrategy;

  }

  @Override
  protected void process() {
    try {
      Thread.sleep(this.sleepInterval);
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
    if ((System.currentTimeMillis() > this.dieAt) && !this.shutdownRequested()) {
      LOG.fine("Hard time out after "
          + (System.currentTimeMillis() - this.startTime) + "ms. "
          + "Allowed time was " + allowedTime() + " Exiting.");
      this.exitStrategy.apply();
      this.requestStop();
    }
  }

  private long allowedTime() {
    return (this.dieAt - this.startTime);
  }

}
