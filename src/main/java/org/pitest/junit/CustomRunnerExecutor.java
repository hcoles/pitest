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

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.pitest.extension.ResultCollector;

public class CustomRunnerExecutor {

  private final Runner                              runner;
  private ResultCollector                           rc;
  private final Map<String, org.pitest.Description> descriptionLookup;

  public CustomRunnerExecutor(final Runner runner, final ResultCollector rc,
      final Map<String, org.pitest.Description> descriptionLookup) {
    this.runner = runner;
    this.rc = rc;
    this.descriptionLookup = descriptionLookup;

  }

  public void run() {

    final RunNotifier rn = new RunNotifier();
    final RunListener listener = new RunListener() {
      private final Set<String> finished = new TreeSet<String>();

      @Override
      public void testFailure(final Failure failure) throws Exception {
        final String d = descriptionToString(failure.getDescription());
        CustomRunnerExecutor.this.rc.notifyEnd(convertDescription(d), failure
            .getException());
        this.finished.add(d);
      }

      @Override
      public void testAssumptionFailure(final Failure failure) {
        final String d = descriptionToString(failure.getDescription());

        CustomRunnerExecutor.this.rc.notifyEnd(convertDescription(d), failure
            .getException());
        this.finished.add(d);
      }

      @Override
      public void testIgnored(final Description description) throws Exception {
        final String d = descriptionToString(description);

        CustomRunnerExecutor.this.rc.notifySkipped(convertDescription(d));

        this.finished.add(d);

      }

      @Override
      public void testStarted(final Description description) throws Exception {
        CustomRunnerExecutor.this.rc
            .notifyStart(convertDescription(descriptionToString(description)));
      }

      @Override
      public void testFinished(final Description description) throws Exception {
        final String d = descriptionToString(description);
        if (!this.finished.contains(d)) {
          CustomRunnerExecutor.this.rc.notifyEnd(convertDescription(d));
        }

      }

    };
    rn.addFirstListener(listener);
    this.runner.run(rn);

  }

  private org.pitest.Description convertDescription(final String desc) {
    final org.pitest.Description description = this.descriptionLookup.get(desc);
    if (description == null) {
      return new org.pitest.Description(desc, CustomRunnerExecutor.class, null);
    } else {
      return description;
    }
  }

  public static String descriptionToString(final Description d) {
    final StringBuffer b = new StringBuffer();
    descriptionToString(b, d);
    return b.toString();
  }

  private static void descriptionToString(final StringBuffer b,
      final Description d) {
    for (final Description each : d.getChildren()) {
      descriptionToString(b, each);
    }
    b.append(d.getMethodName());
  }

  public ResultCollector getRc() {
    return this.rc;
  }

  public void setRc(final ResultCollector rc) {
    this.rc = rc;
  }

}
