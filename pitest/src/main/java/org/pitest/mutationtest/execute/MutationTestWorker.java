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

import org.pitest.ConcreteConfiguration;
import org.pitest.DefaultStaticConfig;
import org.pitest.MultipleTestGroup;
import org.pitest.Pitest;
import org.pitest.containers.UnContainer;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F2;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ConcreteResultCollector;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.DefaultPITClassloader;
import org.pitest.mutationtest.CheckTestHasFailedResultListener;
import org.pitest.mutationtest.ExitingResultCollector;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.MutationTimingListener;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.TimeOutDecoratedTestSource;
import org.pitest.mutationtest.mocksupport.JavassistInterceptor;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.util.Log;

public class MutationTestWorker {

  private final static Logger                   LOG = Log.getLogger();
  protected final Mutater                       mutater;
  protected final ClassLoader                   loader;
  protected final F2<Class<?>, byte[], Boolean> hotswap;

  public MutationTestWorker(final F2<Class<?>, byte[], Boolean> hotswap,
      final Mutater mutater, final ClassLoader loader) {
    this.loader = loader;
    this.mutater = mutater;
    this.hotswap = hotswap;
  }

  protected void run(final Collection<MutationDetails> range, final Reporter r,
      final TimeOutDecoratedTestSource testSource) throws IOException,
      ClassNotFoundException {

    // System.out.println("Mutating class " + classesToMutate);

    for (final MutationDetails i : range) {
      LOG.info("Running mutation " + i);
      final long t0 = System.currentTimeMillis();
      processMutation(r, testSource, i);
      LOG.fine("processed mutation in " + (System.currentTimeMillis() - t0)
          + " ms.");
    }

  }

  private void processMutation(final Reporter r,
      final TimeOutDecoratedTestSource testSource,
      final MutationDetails mutationDetails) throws IOException,
      ClassNotFoundException {

    final MutationIdentifier mutationId = mutationDetails.getId();
    final Mutant mutatedClass = this.mutater.getMutation(mutationId);

    // For the benefit of mocking frameworks such as PowerMock
    // mess with the internals of Javassist so our mutated class
    // bytes are returned
    JavassistInterceptor.setMutant(mutatedClass);

    LOG.fine("mutating method " + mutatedClass.getDetails().getMethod());

    final List<TestUnit> relevantTests = testSource
    .translateTests(mutationDetails.getTestsInOrder());
    // pickTests(mutatedClass);

    r.describe(mutationId);

    MutationStatusTestPair mutationDetected = new MutationStatusTestPair(
        DetectionStatus.SURVIVED);
    if ((relevantTests == null) || relevantTests.isEmpty()) {
      LOG.info("No test coverage for mutation  " + mutationId + " in "
          + mutatedClass.getDetails().getMethod());
      mutationDetected =  new MutationStatusTestPair(
          DetectionStatus.NO_COVERAGE);
    } else {
      LOG.info("" + relevantTests.size() + " relevant test for "
          + mutatedClass.getDetails().getMethod());

      final ClassLoader activeloader = pickClassLoaderForMutant(mutatedClass);
      final Container c = createNewContainer(activeloader);
      final Class<?> testee = Class.forName(mutationId.getClazz(), false,
          activeloader);

      final long t0 = System.currentTimeMillis();
      if (this.hotswap.apply(testee, mutatedClass.getBytes())) {
        LOG.fine("replaced class with mutant in "
            + (System.currentTimeMillis() - t0) + " ms");
        mutationDetected = doTestsDetectMutation(c, relevantTests);
      } else {
        LOG.info("Mutation " + mutationId + " was not viable ");
        mutationDetected = new MutationStatusTestPair(
            DetectionStatus.NON_VIABLE);
      }

    }

    r.report(mutationId, mutationDetected);

    LOG.info("Mutation " + mutationId + " detected = " + mutationDetected);
  }

  private Container createNewContainer(final ClassLoader activeloader) {
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
      LOG.info("Creating new classloader for static initializer");
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

  protected MutationStatusTestPair doTestsDetectMutation(final Container c,
      final List<TestUnit> tests) {
    try {
      final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener();

      final ConcreteConfiguration conf = new ConcreteConfiguration();

      final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
      staticConfig.addTestListener(listener);
      staticConfig.addTestListener(new MutationTimingListener(System.out));

      final Pitest pit = new Pitest(staticConfig, conf);
      pit.run(c, createEarlyExitTestGroup(tests));

      return createStatusTestPair(listener);
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private MutationStatusTestPair createStatusTestPair(
      final CheckTestHasFailedResultListener listener) {
    if (listener.lastFailingTest().hasSome()) {
      return new MutationStatusTestPair(listener.status(), listener
          .lastFailingTest().value().getQualifiedName());
    } else {
      return new MutationStatusTestPair(listener.status());
    }
  }

  private List<TestUnit> createEarlyExitTestGroup(final List<TestUnit> tests) {
    return Collections.<TestUnit> singletonList(new MultipleTestGroup(tests));
  }

}
