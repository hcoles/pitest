package org.pitest.util;

public abstract class AbstractMonitor extends Thread implements Monitor {

  private volatile boolean shouldRun = true;

  public AbstractMonitor() {
    setDaemon(true);
  }

  public void requestStart() {
    start();
  }

  @Override
  public void run() {
    while (this.shouldRun) {
      process();
    }
  }

  public void requestStop() {
    this.shouldRun = false;
    this.interrupt();
  }

  protected boolean shutdownRequested() {
    return !this.shouldRun;
  }

  protected abstract void process();

  public final void waitForExit(final long timeOutInMs) {
    try {
      this.join(timeOutInMs);
    } catch (final InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
