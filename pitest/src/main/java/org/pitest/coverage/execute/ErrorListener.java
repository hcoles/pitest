package org.pitest.coverage.execute;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.pitest.testapi.Description;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;
import org.pitest.util.Log;

public class ErrorListener implements TestListener {
  private static final Logger LOG = Log.getLogger();

  @Override
  public void onRunStart() {
  }

  @Override
  public void onTestStart(final Description d) {
  }

  @Override
  public void onTestFailure(final TestResult tr) {
    LOG.log(Level.SEVERE, tr.getDescription().toString(), tr.getThrowable());
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
