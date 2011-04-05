package org.pitest.coverage;

import org.pitest.coverage.execute.InvokeReceiver;

public interface CoverageReceiver extends InvokeReceiver {

  public abstract void recordTest(int testIndex);

  public abstract void recordTestOutcome(boolean wasGreen, long executionTime);

}