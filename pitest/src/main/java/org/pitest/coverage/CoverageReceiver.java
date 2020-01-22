package org.pitest.coverage;

import org.pitest.testapi.Description;

import sun.pitest.InvokeReceiver;

public interface CoverageReceiver extends InvokeReceiver {

  void recordTestOutcome(Description description, boolean wasGreen,
      int executionTime);

}