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

import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestNGAdapter implements ITestListener {

  private final ResultCollector rc;
  private final Description     description;
  private final Class<?>        clazz;
  private boolean               hasHadFailure = false;
  private Throwable             error;

  public TestNGAdapter(final Class<?> clazz, final Description d,
      final ResultCollector rc) {
    this.rc = rc;
    this.description = d;
    this.clazz = clazz;
  }
  
  public boolean hasHadFailure() {
    return hasHadFailure;
  }

  @Override
  public void onFinish(final ITestContext arg0) {
    if (this.error != null) {
      this.rc.notifyEnd(this.description, this.error);
    } else {
      this.rc.notifyEnd(this.description);
    }
  }

  @Override
  public void onStart(final ITestContext arg0) {
    this.rc.notifyStart(this.description);
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(final ITestResult arg0) {
    // is this success or failure?
    this.rc.notifyEnd(makeDescription(arg0));
  }

  @Override
  public void onTestFailure(final ITestResult arg0) {
    this.hasHadFailure = true;
    this.error = arg0.getThrowable();
    this.rc.notifyEnd(makeDescription(arg0), this.error);
  }

  @Override
  public void onTestSkipped(final ITestResult arg0) {
    this.rc.notifySkipped(makeDescription(arg0));
  }

  @Override
  public void onTestStart(final ITestResult arg0) {
    this.rc.notifyStart(makeDescription(arg0));
  }

  @Override
  public void onTestSuccess(final ITestResult arg0) {
    this.rc.notifyEnd(makeDescription(arg0));
  }

  private Description makeDescription(final ITestResult result) {
    return new Description(result.getMethod().getMethodName(), this.clazz);
  }

}
