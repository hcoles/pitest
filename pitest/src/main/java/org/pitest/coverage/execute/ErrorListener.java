package org.pitest.coverage.execute;

import org.pitest.testapi.Description;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;

public class ErrorListener implements TestListener {

  public void onRunStart() {
  }

  public void onTestStart(final Description d) {
  }

  public void onTestFailure(final TestResult tr) {
    System.out.println("FAIL " + tr.getDescription() + " -> "
        + tr.getThrowable());
  }

  public void onTestError(final TestResult tr) {
    System.out.println("ERROR " + tr.getDescription() + " -> "
        + tr.getThrowable());
    tr.getThrowable().printStackTrace();
  }

  public void onTestSkipped(final TestResult tr) {
  }

  public void onTestSuccess(final TestResult tr) {

  }

  public void onRunEnd() {
  }

}
