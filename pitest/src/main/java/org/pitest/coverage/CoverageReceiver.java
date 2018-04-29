package org.pitest.coverage;

import org.pitest.testapi.Description;

import sun.pitest.InvokeReceiver;

public interface CoverageReceiver extends InvokeReceiver {

  void newTest();

  void recordTestOutcome(Description description, boolean wasGreen,
      int executionTime);

}