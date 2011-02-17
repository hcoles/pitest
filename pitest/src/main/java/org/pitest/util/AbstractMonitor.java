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

  /*
   * (non-Javadoc)
   * 
   * @see org.pitest.util.Monitor#requestStop()
   */
  public void requestStop() {
    this.shouldRun = false;
  }

  protected boolean shutdownRequested() {
    return !this.shouldRun;
  }

  protected abstract void process();

}
