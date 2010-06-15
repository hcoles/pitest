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
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.pitest.TestResult;
import org.pitest.extension.TestListener;
import org.pitest.extension.TestUnit;

/**
 * @author henry
 * 
 */
public class JUnitTestResultListener implements TestListener {

  private final RunNotifier notifier;

  public static Description methodToDescription(final TestResult tr) {
    return unitToDescription(tr.getTest());
  }

  public static Description unitToDescription(final TestUnit tu) {
    return Description.createTestDescription(tu.description().getTestClass(),
        tu.description().getName());
  }

  public JUnitTestResultListener(final RunNotifier notifier) {
    this.notifier = notifier;
  }

  public void onTestError(final TestResult tr) {
    final Failure failure = new Failure(methodToDescription(tr), tr
        .getThrowable());
    this.notifier.fireTestFailure(failure);
  }

  public void onTestFailure(final TestResult tr) {
    final Failure failure = new Failure(methodToDescription(tr), tr
        .getThrowable());
    this.notifier.fireTestFailure(failure);
  }

  public void onTestSkipped(final TestResult tr) {
    this.notifier.fireTestIgnored(methodToDescription(tr));
  }

  public void onTestSuccess(final TestResult tr) {
    this.notifier.fireTestFinished(methodToDescription(tr));
  }

  public void onTestStart(final TestUnit tu) {
    this.notifier.fireTestStarted(unitToDescription(tu));
  }

}
