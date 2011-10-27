/*
 * Copyright 2011 Henry Coles
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
package org.pitest.extension;

import org.pitest.Description;
import org.pitest.TestResult;

public class CompoundTestListener implements TestListener {

  private final Iterable<TestListener> children;

  public CompoundTestListener(final Iterable<TestListener> children) {
    this.children = children;
  }

  public void onRunStart() {
    for (final TestListener each : this.children) {
      each.onRunStart();
    }
  }

  public void onTestStart(final Description d) {
    for (final TestListener each : this.children) {
      each.onTestStart(d);
    }
  }

  public void onTestFailure(final TestResult tr) {
    for (final TestListener each : this.children) {
      each.onTestFailure(tr);
    }
  }

  public void onTestError(final TestResult tr) {
    for (final TestListener each : this.children) {
      each.onTestError(tr);
    }
  }

  public void onTestSkipped(final TestResult tr) {
    for (final TestListener each : this.children) {
      each.onTestSkipped(tr);
    }
  }

  public void onTestSuccess(final TestResult tr) {
    for (final TestListener each : this.children) {
      each.onTestSuccess(tr);
    }
  }

  public void onRunEnd() {
    for (final TestListener each : this.children) {
      each.onRunEnd();
    }
  }

}
