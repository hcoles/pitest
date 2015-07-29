package org.pitest.coverage.execute;

import org.pitest.testapi.Description;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;

public class ErrorListener implements TestListener {

  @Override
  public void onRunStart() {
  }

  @Override
  public void onTestStart(final Description d) {
  }

  @Override
  public void onTestFailure(final TestResult tr) {
    System.out.println("FAIL " + tr.getDescription() + " -> "
        + tr.getThrowable());
  }

  @Override
  public void onTestSkipped(final TestResult tr) {
  }

  @Override
  public void onTestSuccess(final TestResult tr) {

  }

  @Override
  public void onRunEnd() {
  }

}
