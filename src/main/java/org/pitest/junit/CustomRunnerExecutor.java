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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.pitest.extension.ResultCollector;
import org.pitest.junit.adapter.RunnerAdapterDescriptionTestUnit;

public class CustomRunnerExecutor {

  private final Runner                              runner;
  private final ResultCollector                     rc;
  private final Map<String, org.pitest.Description> descriptionLookup;

  public CustomRunnerExecutor(final Runner runner, final ResultCollector rc,
      final List<RunnerAdapterDescriptionTestUnit> descriptions) {
    this.runner = runner;
    this.rc = rc;
    this.descriptionLookup = new HashMap<String, org.pitest.Description>();
    for (final RunnerAdapterDescriptionTestUnit each : descriptions) {
      // map against string representation of description to avoid
      // equality issues with multiple classloaders
      this.descriptionLookup.put(
          descriptionToString(each.getJunitDescription()), each.description());
    }
  }

  public void run() {
    // map against string representation of description to avoid
    // equality issues with multiple classloaders
    final RunNotifier rn = new RunNotifier();
    final RunListener listener = new RunListener() {

      @Override
      public void testFailure(final Failure failure) throws Exception {
        CustomRunnerExecutor.this.rc.notifyEnd(
            CustomRunnerExecutor.this.descriptionLookup
                .get(descriptionToString(failure.getDescription())), failure
                .getException());
      }

      @Override
      public void testAssumptionFailure(final Failure failure) {
        CustomRunnerExecutor.this.rc.notifyEnd(
            CustomRunnerExecutor.this.descriptionLookup
                .get(descriptionToString(failure.getDescription())), failure
                .getException());
      }

      @Override
      public void testIgnored(final Description description) throws Exception {
        CustomRunnerExecutor.this.rc
            .notifySkipped(CustomRunnerExecutor.this.descriptionLookup
                .get(descriptionToString(description)));

      }

      @Override
      public void testStarted(final Description description) throws Exception {
        CustomRunnerExecutor.this.rc
            .notifyStart(CustomRunnerExecutor.this.descriptionLookup
                .get(descriptionToString(description)));
      }

      @Override
      public void testFinished(final Description description) throws Exception {
        CustomRunnerExecutor.this.rc
            .notifyEnd(CustomRunnerExecutor.this.descriptionLookup
                .get(descriptionToString(description)));
      }

    };
    rn.addFirstListener(listener);
    this.runner.run(rn);

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

}
