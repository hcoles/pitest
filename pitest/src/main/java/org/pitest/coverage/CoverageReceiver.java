package org.pitest.coverage;

import org.pitest.Description;
import org.pitest.boot.InvokeReceiver;

public interface CoverageReceiver extends InvokeReceiver {

  public abstract void newTest();

  public abstract void recordTestOutcome(Description description,
      boolean wasGreen, long executionTime);

}