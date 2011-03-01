package org.pitest.coverage.execute;

import org.pitest.coverage.CoverageStatistics;
import org.pitest.coverage.InvokeEntry;
import org.pitest.coverage.InvokeQueue;

public class CoverageReaderThread extends Thread {

  private volatile boolean         shouldRun = true;

  private final InvokeQueue        invokeQueue;
  private final CoverageStatistics invokeStatistics;

  public CoverageReaderThread(final InvokeQueue invokeQueue,
      final CoverageStatistics invokeStatistics) {
    this.invokeQueue = invokeQueue;
    this.invokeStatistics = invokeStatistics;
    this.setDaemon(true);
  }

  @Override
  public void run() {

    while (this.shouldRun || !this.invokeQueue.isEmpty()) {
      try {
        readStatisticsQueue(100);
      } catch (final InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public void waitToFinish() throws InterruptedException {
    this.shouldRun = false;
    this.join();
  }

  private void readStatisticsQueue(final int timeout)
      throws InterruptedException {

    for (final InvokeEntry entry : this.invokeQueue.poll(100)) {
      if (entry != null) {
        this.invokeStatistics.visitLine(entry.getClassId(),
            entry.getLineNumber());
      }

    }

  }

}
