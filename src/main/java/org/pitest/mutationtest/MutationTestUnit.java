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

import static org.pitest.util.Unchecked.translateCheckedException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassLoaderRepository;
import org.pitest.ConcreteConfiguration;
import org.pitest.Description;
import org.pitest.Pitest;
import org.pitest.extension.Configuration;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.EmptyConfiguration;
import org.pitest.internal.ClassPath;
import org.pitest.internal.classloader.OtherClassLoaderClassPathRoot;
import org.pitest.mutationtest.loopbreak.LoopBreakTestUnitProcessor;
import org.pitest.testunit.AbstractTestUnit;

import com.reeltwo.jumble.mutation.Mutater;

public class MutationTestUnit extends AbstractTestUnit {

  private static final Logger  logger = Logger.getLogger(MutationTestUnit.class
                                          .getName());

  private final Class<?>       test;
  private final Class<?>       classToMutate;

  private final MutationConfig config;
  private final Configuration  pitConfig;

  public MutationTestUnit(final Class<?> test, final Class<?> classToMutate,
      final MutationConfig mutationConfig, final Configuration pitConfig,
      final Description description) {
    super(description, null);
    this.classToMutate = classToMutate;
    this.test = test;
    this.config = mutationConfig;
    this.pitConfig = pitConfig;
  }

  private List<TestUnit> findTestUnits(final long timeOut) {
    final Configuration updatedConfig = createCopyOfConfig(this.pitConfig);

    if (timeOut >= 0) {
      System.out.println("Adding loop break");
      updatedConfig.testUnitProcessors().add(
          new LoopBreakTestUnitProcessor(this.getMaxDuration(timeOut)));
    }
    return Pitest.findTestUnitsForAllSuppliedClasses(updatedConfig, this.test);
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    try {
      rc.notifyStart(this.description());
      runTests(rc, loader);
    } catch (final Throwable ex) {
      rc.notifyEnd(this.description(), ex);
    }

  }

  private long getMaxDuration(final long testDuration) {
    return (testDuration * 2) + 50;
  }

  private void runTests(final ResultCollector rc, final ClassLoader loader) {

    final Mutater m = this.config.createMutator();
    m.setRepository(new ClassLoaderRepository(loader));
    final String name = this.classToMutate.getName();

    final int mutationCount = m.countMutationPoints(name);

    try {
      if (mutationCount > 0) {

        // m.setMutationPoint(0);
        final long normalExecution = timeUnmutatedTests(m
            .jumbler(this.classToMutate.getName()), findTestUnits(-1), loader);

        final List<TestUnit> tests = findTestUnits(normalExecution);
        final List<AssertionError> failures = new ArrayList<AssertionError>();

        for (int i = 0; i != mutationCount; i++) {

          m.setMutationPoint(i);
          final JavaClass mutatedClass = m
              .jumbler(this.classToMutate.getName());

          if (!doTestsDetectMutation(loader, mutatedClass, tests)) {

            final MutationDetails details = new MutationDetails(mutatedClass
                .getClassName(), mutatedClass.getFileName(), m
                .getModification(), m.getMutatedMethodName(this.classToMutate
                .getName()));

            failures.add(this.createAssertionError(details));

          }
        }

        reportResults(mutationCount, failures, rc);

      } else {
        logger.info("Skipping test " + this.description()
            + " as no mutations found");
        rc.notifySkipped(this.description());
      }
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private void reportResults(final int mutationCount,
      final List<AssertionError> failures, final ResultCollector rc) {
    final float percentageDetected = 100f - ((failures.size() / (float) mutationCount) * 100f);
    if (percentageDetected < this.config.getThreshold()) {

      final AssertionError ae = new AssertionError("Tests detected "
          + percentageDetected + "% of " + mutationCount
          + " mutations. Threshold was " + this.config.getThreshold());
      AssertionError last = ae;
      for (final AssertionError each : failures) {
        last.initCause(each);
        last = each;
      }
      rc.notifyEnd(this.description(), ae);
    } else {
      rc.notifyEnd(this.description());
    }

  }

  private long timeUnmutatedTests(final JavaClass unmutatedClass,
      final List<TestUnit> list, final ClassLoader loader) {
    final long t0 = System.currentTimeMillis();
    if (doTestsDetectMutation(loader, unmutatedClass, list)) {
      throw new RuntimeException(
          "Cannot mutation test as tests do not pass without mutation");
    }
    return System.currentTimeMillis() - t0;
  }

  private boolean doTestsDetectMutation(final ClassLoader loader,
      final JavaClass mutatedClass, final List<TestUnit> tests) {
    try {
      final MutationTestResultListener listener = new CheckTestHasFailedResultListener();
      final ClassPath classPath = new ClassPath(
          new OtherClassLoaderClassPathRoot(loader));

      final JumbleContainer c = new JumbleContainer(classPath, mutatedClass);

      // why use empty config here? why not the updated one?
      final Pitest pit = new Pitest(c, new EmptyConfiguration());

      pit.addListener(listener);

      // FIXME should abort test run as soon as 1 test fails
      pit.run(c, tests);

      return listener.resultIndicatesSuccess();
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private AssertionError createAssertionError(final MutationDetails md) {
    final AssertionError ae = new AssertionError("The mutation -> " + md
        + " did not result in any test failures");
    final StackTraceElement[] stackTrace = { md.stackTraceDescription() };
    ae.setStackTrace(stackTrace);
    return ae;
  }

  private Configuration createCopyOfConfig(final Configuration configuration) {
    return new ConcreteConfiguration(configuration);
  }

  public MutationConfig getMutationConfig() {
    return this.config;
  }

}
