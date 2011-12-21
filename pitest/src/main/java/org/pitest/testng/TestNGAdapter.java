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
package org.pitest.testng;

import org.pitest.Description;
import org.pitest.extension.ResultCollector;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestNGAdapter implements ITestListener {

  private final ResultCollector rc;
  private final Description     description;

  public TestNGAdapter(final Description d, final ResultCollector rc) {
    this.rc = rc;
    this.description = d;
  }

  public void onFinish(final ITestContext arg0) {
  }

  public void onStart(final ITestContext arg0) {

  }

  public void onTestFailedButWithinSuccessPercentage(final ITestResult arg0) {
    // is this success or failure?
    this.rc.notifyEnd(this.description);
  }

  public void onTestFailure(final ITestResult arg0) {
    this.rc.notifyEnd(this.description, arg0.getThrowable());
  }

  public void onTestSkipped(final ITestResult arg0) {
    this.rc.notifySkipped(this.description);
  }

  public void onTestStart(final ITestResult arg0) {
    this.rc.notifyStart(this.description);
  }

  public void onTestSuccess(final ITestResult arg0) {
    this.rc.notifyEnd(this.description);
  }

}
