package org.pitest.coverage;


import org.pitest.testapi.Description;

import sun.pitest.InvokeReceiver;

public interface CoverageReceiver extends InvokeReceiver {

  public abstract void newTest();

  public abstract void recordTestOutcome(Description description,
      boolean wasGreen, int executionTime);

}