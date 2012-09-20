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
package org.pitest.mutationtest.execute;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.pitest.DefaultStaticConfig;
import org.pitest.MultipleTestGroup;
import org.pitest.Pitest;
import org.pitest.containers.UnContainer;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F3;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ConcreteResultCollector;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.DefaultPITClassloader;
import org.pitest.mutationtest.CheckTestHasFailedResultListener;
import org.pitest.mutationtest.ExitingResultCollector;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.TimeOutDecoratedTestSource;
import org.pitest.mutationtest.mocksupport.JavassistInterceptor;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.util.Log;

public class MutationTestWorker {

  private static final Logger                            LOG = Log.getLogger();
  private final Mutater                                  mutater;
  private final ClassLoader                              loader;
  private final F3<String, ClassLoader, byte[], Boolean> hotswap;

  public MutationTestWorker(
      final F3<String, ClassLoader, byte[], Boolean> hotswap,
      final Mutater mutater, final ClassLoader loader) {
    this.loader = loader;
    this.mutater = mutater;
    this.hotswap = hotswap;
  }

  protected void run(final Collection<MutationDetails> range, final Reporter r,
      final TimeOutDecoratedTestSource testSource) throws IOException {

    for (final MutationDetails mutation : range) {
      LOG.fine("Running mutation " + mutation);
      final long t0 = System.currentTimeMillis();
      processMutation(r, testSource, mutation);
      LOG.fine("processed mutation in " + (System.currentTimeMillis() - t0)
          + " ms.");
    }

  }

  private void processMutation(final Reporter r,
      final TimeOutDecoratedTestSource testSource,
      final MutationDetails mutationDetails) throws IOException {

    final MutationIdentifier mutationId = mutationDetails.getId();
    final Mutant mutatedClass = this.mutater.getMutation(mutationId);

    // For the benefit of mocking frameworks such as PowerMock
    // mess with the internals of Javassist so our mutated class
    // bytes are returned
    JavassistInterceptor.setMutant(mutatedClass);

    LOG.fine("mutating method " + mutatedClass.getDetails().getMethod());

    final List<TestUnit> relevantTests = testSource
        .translateTests(mutationDetails.getTestsInOrder());

    r.describe(mutationId);

    final MutationStatusTestPair mutationDetected = handleMutation(mutationId,
        mutatedClass, relevantTests);

    r.report(mutationId, mutationDetected);

    LOG.fine("Mutation " + mutationId + " detected = " + mutationDetected);
  }

  private MutationStatusTestPair handleMutation(
      final MutationIdentifier mutationId, final Mutant mutatedClass,
      final List<TestUnit> relevantTests) {
    MutationStatusTestPair mutationDetected;
    if ((relevantTests == null) || relevantTests.isEmpty()) {
      LOG.info("No test coverage for mutation  " + mutationId + " in "
          + mutatedClass.getDetails().getMethod());
      mutationDetected = new MutationStatusTestPair(0,
          DetectionStatus.RUN_ERROR);
    } else {
      mutationDetected = handleCoveredMutation(mutationId, mutatedClass,
          relevantTests);

    }
    return mutationDetected;
  }

  private MutationStatusTestPair handleCoveredMutation(
      final MutationIdentifier mutationId, final Mutant mutatedClass,
      final List<TestUnit> relevantTests) {
    MutationStatusTestPair mutationDetected;
    LOG.fine("" + relevantTests.size() + " relevant test for "
        + mutatedClass.getDetails().getMethod());

    final ClassLoader activeloader = pickClassLoaderForMutant(mutatedClass);
    final Container c = createNewContainer(activeloader);
    final long t0 = System.currentTimeMillis();
    if (this.hotswap.apply(mutationId.getClazz(), activeloader,
        mutatedClass.getBytes())) {
      LOG.fine("replaced class with mutant in "
          + (System.currentTimeMillis() - t0) + " ms");
      mutationDetected = doTestsDetectMutation(c, relevantTests);
    } else {
      LOG.warning("Mutation " + mutationId + " was not viable ");
      mutationDetected = new MutationStatusTestPair(0,
          DetectionStatus.NON_VIABLE);
    }
    return mutationDetected;
  }

  private static Container createNewContainer(final ClassLoader activeloader) {
    final Container c = new UnContainer() {
      @Override
      public void submit(final TestUnit group) {
        final ExitingResultCollector rc = new ExitingResultCollector(
            new ConcreteResultCollector(this.feedbackQueue));
        group.execute(activeloader, rc);
      }
    };
    return c;
  }

  private ClassLoader pickClassLoaderForMutant(final Mutant mutant) {
    if (hasMutationInStaticInitializer(mutant)) {
      LOG.fine("Creating new classloader for static initializer");
      return new DefaultPITClassloader(new ClassPath(),
          IsolationUtils.bootClassLoader());
    } else {
      return this.loader;
    }
  }

  private boolean hasMutationInStaticInitializer(final Mutant mutant) {
    return mutant.getDetails().isInStaticInitializer();
  }

  @Override
  public String toString() {
    return "MutationTestWorker [mutater=" + this.mutater + ", loader="
        + this.loader + ", hotswap=" + this.hotswap + "]";
  }

  private MutationStatusTestPair doTestsDetectMutation(final Container c,
      final List<TestUnit> tests) {
    try {
      final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener();

      final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
      staticConfig.addTestListener(listener);

      final Pitest pit = new Pitest(staticConfig);
      pit.run(c, createEarlyExitTestGroup(tests));

      return createStatusTestPair(listener);
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private MutationStatusTestPair createStatusTestPair(
      final CheckTestHasFailedResultListener listener) {
    if (listener.lastFailingTest().hasSome()) {
      return new MutationStatusTestPair(listener.getNumberOfTestsRun(),
          listener.status(), listener.lastFailingTest().value()
              .getQualifiedName());
    } else {
      return new MutationStatusTestPair(listener.getNumberOfTestsRun(),
          listener.status());
    }
  }

  private List<TestUnit> createEarlyExitTestGroup(final List<TestUnit> tests) {
    return Collections.<TestUnit> singletonList(new MultipleTestGroup(tests));
  }

}
