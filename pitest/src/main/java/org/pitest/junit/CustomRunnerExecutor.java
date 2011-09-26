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
package org.pitest.junit;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.pitest.extension.ResultCollector;

public class CustomRunnerExecutor {

  private final org.pitest.Description description;
  private final Runner                 runner;
  private ResultCollector              rc;

  public CustomRunnerExecutor(final org.pitest.Description description,
      final Runner runner, final ResultCollector rc) {
    this.runner = runner;
    this.rc = rc;
    this.description = description;
  }

  public void run() {

    final RunNotifier rn = new RunNotifier();
    final RunListener listener = new RunListener() {

      private boolean finished = false;

      @Override
      public void testFailure(final Failure failure) throws Exception {

        CustomRunnerExecutor.this.rc.notifyEnd(
            CustomRunnerExecutor.this.description, failure.getException());
        this.finished = true;
      }

      @Override
      public void testAssumptionFailure(final Failure failure) {
        // FIXME should assumption failures not be mapped to skipped?
        CustomRunnerExecutor.this.rc.notifyEnd(
            CustomRunnerExecutor.this.description, failure.getException());
        this.finished = true;
      }

      @Override
      public void testIgnored(final Description description) throws Exception {

        CustomRunnerExecutor.this.rc
            .notifySkipped(CustomRunnerExecutor.this.description);
        this.finished = true;

      }

      @Override
      public void testStarted(final Description description) throws Exception {
        CustomRunnerExecutor.this.rc
            .notifyStart(CustomRunnerExecutor.this.description);
      }

      @Override
      public void testFinished(final Description description) throws Exception {
        // final String d = descriptionToString(description);
        if (!this.finished) {
          CustomRunnerExecutor.this.rc
              .notifyEnd(CustomRunnerExecutor.this.description);
        }

      }

    };
    rn.addFirstListener(listener);
    this.runner.run(rn);

  }

  public ResultCollector getRc() {
    return this.rc;
  }

  public void setRc(final ResultCollector rc) {
    this.rc = rc;
  }

}
