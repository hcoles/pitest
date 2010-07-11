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
import org.pitest.extension.TestListener;
import org.pitest.functional.Option;

public class CheckAllTestsPassedResultsListener implements TestListener,
    MutationTestResultListener {

  private static final long serialVersionUID     = 1L;

  private boolean           atLeastOneTestFailed = false;
  private TestResult        error;

  public boolean resultIndicatesSuccess() {
    return !this.atLeastOneTestFailed;
  }

  public void onTestError(final TestResult tr) {
    this.error = tr;
    this.atLeastOneTestFailed = true;
  }

  public void onTestFailure(final TestResult tr) {
    this.atLeastOneTestFailed = true;

  }

  public void onTestSkipped(final TestResult tr) {
    // TODO Auto-generated method stub

  }

  public void onTestStart(final Description d) {
    // TODO Auto-generated method stub

  }

  public void onTestSuccess(final TestResult tr) {
    // TODO Auto-generated method stub

  }

  public Option<TestResult> error() {
    return Option.someOrNone(this.error);
  }
}
