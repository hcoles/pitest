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
package org.pitest.extension.common;

import org.pitest.Description;
import org.pitest.TestResult;
import org.pitest.extension.TestListener;

/**
 * @author henry
 * 
 */
public class ConsoleResultListener implements TestListener {

  private static final long                  serialVersionUID = 1L;

  private final static ConsoleResultListener instance         = new ConsoleResultListener();

  public static ConsoleResultListener instance() {
    return instance;
  }

  public void onTestFailure(final TestResult tr) {
    System.out.println("Failed " + tr);
  }

  public void onTestSkipped(final TestResult tr) {
    System.out.println("Skipped " + tr);
  }

  public void onTestSuccess(final TestResult tr) {
    System.out.println("Success " + tr);
  }

  public void onTestError(final TestResult tr) {
    System.out.println("Error " + tr);

  }

  public void onTestStart(final Description d) {
    System.out.println("Started " + d);
  }

}
