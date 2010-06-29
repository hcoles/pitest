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

import java.util.List;

import org.apache.bcel.classfile.JavaClass;
import org.pitest.Description;
import org.pitest.Pitest;
import org.pitest.TestResult;
import org.pitest.extension.Configuration;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.EmptyConfiguration;
import org.pitest.functional.Option;
import org.pitest.internal.ConfigParser;
import org.pitest.mutationtest.loopbreak.LoopBreakTestUnitProcessor;
import org.pitest.testunit.AbstractTestUnit;

public class MutationTestUnit extends AbstractTestUnit {

  private static final long                serialVersionUID = 1L;

  private boolean                          passed           = false;
  private long                             duration;

  private final List<TestUnit>             tests;

  private final Option<MutationTestUnit>   unmutatedTest;
  // private final Class<?>[] testClasses;
  private final JavaClass                  mutatedClass;
  private final MutationTestResultListener listener;
  private final MutationDetails            details;

  public MutationTestUnit(final MutationTestUnit unmutatedTest,
      final JavaClass mutatedClass, final MutationDetails details,
      final MutationTestResultListener listener, final Class<?>[] tests,
      final Description description, final Configuration config) {
    super(description, unmutatedTest);
    // this.testClasses = tests;
    this.unmutatedTest = Option.someOrNone(unmutatedTest);
    this.mutatedClass = mutatedClass;
    this.listener = listener;
    this.details = details;
    this.tests = findTestUnits(config, tests);
  }

  private List<TestUnit> findTestUnits(final Configuration config,
      final Class<?>[] testClasses) {
    final Configuration updatedConfig = createCopyOfConfig(config);

    updatedConfig.testUnitProcessors().add(
        new LoopBreakTestUnitProcessor(this.getTimeOut()));

    updatedConfig.testUnitFinders().remove(MutationTestUnitFinder.instance());
    return Pitest
        .findTestUnitsForAllSuppliedClasses(updatedConfig, testClasses);
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    final long t0 = System.currentTimeMillis();
    try {
      if (this.unmutatedTest.hasSome() && !this.unmutatedTest.value().passed()) {
        rc.notifySkipped(this);
      } else {
        rc.notifyStart(this);
        runTests(rc);
      }

    } catch (final Throwable ex) {
      rc.notifyEnd(new TestResult(this, ex));
    } finally {
      this.duration = System.currentTimeMillis() - t0;
    }

  }

  private long getTimeOut() {
    if (this.unmutatedTest.hasSome()) {
      return System.currentTimeMillis()
          + (this.unmutatedTest.value().getDuration() * 2) + 200;
    } else {
      return 0;
    }
  }

  private void runTests(final ResultCollector rc) {

    // final Configuration config = createCopyOfParentConfig();

    // config.testUnitProcessors().add(
    // new LoopBreakTestUnitProcessor(this.getTimeOut()));

    // config.testUnitFinders().remove(MutationTestUnitFinder.instance());
    final JumbleContainer c = new JumbleContainer(
        MutationTestUnit.this.mutatedClass);
    final Pitest pit = new Pitest(c, new EmptyConfiguration());

    pit.addListener(this.listener);

    // FIXME should abort test run as soon as 1 test fails
    pit.run(c, this.tests);

    if (this.listener.error().hasSome()) {
      // final Throwable t = new Exception("Error occured in child test",
      // this.listener.error().value().getThrowable());
      // rc.notifyEnd(new TestResult(this, t));
      // for now treat all errors as a succesfully detected mutation
      rc.notifyEnd(new TestResult(this, null));
      this.passed = true;
    } else if (this.listener.resultIndicatesSuccess()) {
      rc.notifyEnd(new TestResult(this, null));
      this.passed = true;
    } else {

      rc.notifyEnd(new TestResult(this, createAssertionError()));
    }

  }

  private Throwable createAssertionError() {
    final AssertionError ae = new AssertionError("The mutation -> "
        + this.details + " did not result in any test failures");
    final StackTraceElement[] stackTrace = { this.details
        .stackTraceDescription() };
    ae.setStackTrace(stackTrace);
    return ae;
  }

  private Configuration createCopyOfConfig(final Configuration configuration) {
    final Configuration config = new ConfigParser(this.getClass())
        .create(configuration);
    return config;
  }

  public boolean passed() {
    return this.passed;
  }

  @Override
  public String toString() {
    return "MutationTestUnit [description()=" + description() + "]";
  }

  public long getDuration() {
    return this.duration;
  }

}
