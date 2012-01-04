/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest;

import org.pitest.Description;
import org.pitest.TestResult;
import org.pitest.TimeoutException;
import org.pitest.extension.TestListener;
import org.pitest.functional.Option;
import org.pitest.mutationtest.results.DetectionStatus;

public class CheckTestHasFailedResultListener implements TestListener {

  private static final long   serialVersionUID = 1L;

  private boolean             timedOut         = false;
  private Option<Description> lastFailingTest  = Option.none();
  private int                 testsRun         = 0;

  public void onTestError(final TestResult tr) {
    recordFailingTest(tr);
    checkForTimeOut(tr);
  }

  private void recordFailingTest(final TestResult tr) {
    this.lastFailingTest = Option.some(tr.getDescription());
  }

  private void checkForTimeOut(final TestResult tr) {
    if (tr.getThrowable() instanceof TimeoutException) {
      this.timedOut = true;
    }
  }

  public void onTestFailure(final TestResult tr) {
    recordFailingTest(tr);
    checkForTimeOut(tr);
  }

  public void onTestSkipped(final TestResult tr) {

  }

  public void onTestStart(final Description d) {
    this.testsRun++;

  }

  public void onTestSuccess(final TestResult tr) {
    // TODO Auto-generated method stub

  }

  public DetectionStatus status() {
    if (this.timedOut) {
      return DetectionStatus.TIMED_OUT;
    } else if (this.lastFailingTest.hasSome()) {
      return DetectionStatus.KILLED;
    } else {
      return DetectionStatus.SURVIVED;
    }
  }

  public Option<Description> lastFailingTest() {
    return this.lastFailingTest;
  }

  public int getNumberOfTestsRun() {
    return this.testsRun;
  }

  public void onRunEnd() {

  }

  public void onRunStart() {

  }

}
