package org.pitest.junit;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.pitest.extension.ResultCollector;

class AdaptingRunListener extends RunListener {

  private final org.pitest.Description description;
  private final ResultCollector        rc;
  private boolean                      finished = false;

  public AdaptingRunListener(org.pitest.Description description,
      ResultCollector rc) {
    this.description = description;
    this.rc = rc;
  }

  @Override
  public void testFailure(final Failure failure) throws Exception {
    rc.notifyEnd(description, failure.getException());
    this.finished = true;
  }

  @Override
  public void testAssumptionFailure(final Failure failure) {
    // do nothing so treated as success
  }

  @Override
  public void testIgnored(final Description description) throws Exception {
    rc.notifySkipped(this.description);
    this.finished = true;

  }

  @Override
  public void testStarted(final Description description) throws Exception {
    rc.notifyStart(this.description);
  }

  @Override
  public void testFinished(final Description description) throws Exception {
    if (!this.finished) {
      rc.notifyEnd(this.description);
    }

  }

}
