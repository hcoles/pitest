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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.pitest.TestMethod;
import org.pitest.TestResult;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.reflection.Reflection;

public class RunnerAdapter {

  private Runner                          runner;
  private final List<TestUnit>            tus     = new ArrayList<TestUnit>();
  private final Map<Description, Failure> results = new HashMap<Description, Failure>();

  public RunnerAdapter(final Runner runner) {
    this.runner = runner;
    gatherTestUnits(this.tus, runner.getDescription());
  }

  public List<TestUnit> getTestUnits() {
    return this.tus;
  }

  private void gatherTestUnits(final List<TestUnit> tus, final Description d) {
    if (d.isTest()) {
      tus.add(descriptionToTestUnit(d));
    } else {
      for (final Description each : d.getChildren()) {
        gatherTestUnits(tus, each);
      }
    }
  }

  private TestUnit descriptionToTestUnit(final Description d) {
    final Method m = Reflection.publicMethod(d.getTestClass(), d
        .getMethodName());
    final TestMethod tm = new TestMethod(m, null);
    final org.pitest.Description pitDescription = new org.pitest.Description(d
        .getDisplayName(), d.getTestClass(), tm);
    return new RunnerAdapterTestUnit(this, d, pitDescription, null);
  }

  public void execute(final ClassLoader loader,
      final RunnerAdapterTestUnit testUnit, final ResultCollector rc) {
    rc.notifyStart(testUnit);
    runIfRequired();
    notify(testUnit, rc);
  }

  private void notify(final RunnerAdapterTestUnit testUnit,
      final ResultCollector rc) {
    final Failure f = this.results.get(testUnit.getJunitDescription());
    if (f != null) {
      final TestResult testResult = new TestResult(testUnit, f.getException());
      rc.notifyEnd(testResult);
    } else {
      final TestResult testResult = new TestResult(testUnit, null);
      rc.notifyEnd(testResult);
    }

  }

  private void runIfRequired() {

    if (this.runner != null) {

      final RunNotifier rn = new RunNotifier();
      final RunListener listener = new RunListener() {

        @Override
        public void testRunStarted(final Description description)
            throws Exception {

        }

        @Override
        public void testRunFinished(final Result result) throws Exception {

        }

        @Override
        public void testStarted(final Description description) throws Exception {

        }

        @Override
        public void testFinished(final Description description)
            throws Exception {

        }

        @Override
        public void testFailure(final Failure failure) throws Exception {

        }

        @Override
        public void testAssumptionFailure(final Failure failure) {

        }

        @Override
        public void testIgnored(final Description description) throws Exception {
          // fixme no support
        }

      };
      rn.addFirstListener(listener);
      this.runner.run(rn);
      this.runner = null;
    }

  }

}
