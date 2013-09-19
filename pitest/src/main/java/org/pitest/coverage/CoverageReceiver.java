package org.pitest.coverage;

import org.pitest.boot.InvokeReceiver;
import org.pitest.testapi.Description;

public interface CoverageReceiver extends InvokeReceiver {

  public abstract void newTest();

  public abstract void recordTestOutcome(Description description,
      boolean wasGreen, int executionTime);

}