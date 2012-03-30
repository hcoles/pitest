package org.pitest.junit.adapter;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.pitest.extension.ResultCollector;

class AdaptingRunListener extends RunListener {

  private final org.pitest.Description description;
  private final ResultCollector        rc;
  private boolean                      finished = false;

  public AdaptingRunListener(final org.pitest.Description description,
      final ResultCollector rc) {
    this.description = description;
    this.rc = rc;
  }

  @Override
  public void testFailure(final Failure failure) throws Exception {
    this.rc.notifyEnd(this.description, failure.getException());
    this.finished = true;
  }

  @Override
  public void testAssumptionFailure(final Failure failure) {
    // do nothing so treated as success
    // see http://junit.sourceforge.net/doc/ReleaseNotes4.4.html#assumptions
  }

  @Override
  public void testIgnored(final Description description) throws Exception {
    this.rc.notifySkipped(this.description);
    this.finished = true;

  }

  @Override
  public void testStarted(final Description description) throws Exception {
    this.rc.notifyStart(this.description);
  }

  @Override
  public void testFinished(final Description description) throws Exception {
    if (!this.finished) {
      this.rc.notifyEnd(this.description);
    }

  }

}
