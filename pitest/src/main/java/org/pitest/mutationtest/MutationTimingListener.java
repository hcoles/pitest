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
package org.pitest.mutationtest;

import java.io.PrintStream;

import org.pitest.Description;
import org.pitest.TestResult;
import org.pitest.extension.TestListener;
import org.pitest.functional.Option;
import org.pitest.mutationtest.execute.TimingMetaData;

public class MutationTimingListener implements TestListener {

  private long accumulatedTime;
  private long tests;

  public MutationTimingListener(final PrintStream stream) {
    this.stream = stream;
  }

  private final PrintStream stream;

  public void onRunStart() {
  }

  public void onTestStart(final Description d) {
    this.tests++;
  }

  public void onTestFailure(final TestResult tr) {
    getTimingMetaData(tr);
    this.stream.print("F");
  }

  public void onTestError(final TestResult tr) {
    getTimingMetaData(tr);
    this.stream.print("E(" + tr.getThrowable().getClass() + ")");
  }

  public void onTestSkipped(final TestResult tr) {

  }

  public void onTestSuccess(final TestResult tr) {
    final long expectedTime = getTimingMetaData(tr);
    this.stream.print("P(" + expectedTime + ")");
  }

  private long getTimingMetaData(final TestResult tr) {
    final Option<TimingMetaData> tmd = tr.getValue(TimingMetaData.class);
    this.accumulatedTime = this.accumulatedTime + tmd.value().getExpectedTime();
    return tmd.value().getExpectedTime();
  }

  public void onRunEnd() {
    this.stream.println(" -> " + this.accumulatedTime + " ms in " + this.tests
        + " tests.");
  }

}
