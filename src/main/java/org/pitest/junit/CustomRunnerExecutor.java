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
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

public class CustomRunnerExecutor {

  private final Runner runner;

  public CustomRunnerExecutor(final Runner runner) {
    this.runner = runner;
  }

  public Map<String, Throwable> run() {
    // map against string representation of description to avoid
    // equality issues with multiple classloaders
    final Map<String, Throwable> results = new HashMap<String, Throwable>();
    final RunNotifier rn = new RunNotifier();
    final RunListener listener = new RunListener() {

      @Override
      public void testFailure(final Failure failure) throws Exception {
        results.put(descriptionToString(failure.getDescription()), failure
            .getException());
      }

      @Override
      public void testAssumptionFailure(final Failure failure) {
        results.put(descriptionToString(failure.getDescription()), failure
            .getException());
      }

      @Override
      public void testIgnored(final Description description) throws Exception {
        // fixme no support
      }

    };
    rn.addFirstListener(listener);
    this.runner.run(rn);

    return results;
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
