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
package org.pitest.mutationtest.execute;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.pitest.functional.Option;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;
import org.pitest.util.Log;

public class CheckTestHasFailedResultListener implements TestListener {

  private static final Logger LOG = Log.getLogger();
  private Option<Description> lastFailingTest = Option.none();
  private Option<Throwable> throwable = Option.none();
  private int                 testsRun        = 0;

  @Override
  public void onTestFailure(final TestResult tr) {
    this.lastFailingTest = Option.some(tr.getDescription());
    this.throwable = Option.some(tr.getThrowable());
  }

  @Override
  public void onTestSkipped(final TestResult tr) {

  }

  @Override
  public void onTestStart(final Description d) {
    this.testsRun++;
  }

  @Override
  public void onTestSuccess(final TestResult tr) {

  }

  public DetectionStatus status() {
    if (this.lastFailingTest.hasSome()) {
      DetectionStatus status = new DetectionStatus();
      status.setActualStatus(DetectionStatus.ActualStatus.KILLED);
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      PrintStream outPS = new PrintStream(outStream);
      if (this.throwable.hasSome()) {
        Throwable t = this.throwable.value();
        t.printStackTrace(outPS);
        Throwable chained = t;
        while (chained != null) {
          if (chained instanceof OutOfMemoryError) {
            status.setActualStatus(DetectionStatus.ActualStatus.MEMORY_ERROR);
            break;
          }
          if (chained instanceof TimeoutException) {
            status.setActualStatus(DetectionStatus.ActualStatus.TIMED_OUT);
            break;
          }
          if (chained.getMessage() != null && chained.getMessage().
              contains("Too many open files")) {
            status.setActualStatus(DetectionStatus.ActualStatus.TOO_MANY_FILES);
            break;
          }
          chained = chained.getCause();
        }
      } else {
        LOG.finer("No stack trace for failed test");
      }
      status.setStackTrace(outStream.toString());
      return status;
    } else {
      return new DetectionStatus(DetectionStatus.SURVIVED);
    }
  }

  public Option<Description> lastFailingTest() {
    return this.lastFailingTest;
  }

  public int getNumberOfTestsRun() {
    return this.testsRun;
  }

  @Override
  public void onRunEnd() {

  }

  @Override
  public void onRunStart() {

  }

}
