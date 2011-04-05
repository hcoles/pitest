package org.pitest.coverage;

import org.pitest.Description;
import org.pitest.coverage.execute.InvokeReceiver;

public interface CoverageReceiver extends InvokeReceiver {

  public abstract void recordTest(Description description);

  public abstract void recordTestOutcome(boolean wasGreen, long executionTime);

}