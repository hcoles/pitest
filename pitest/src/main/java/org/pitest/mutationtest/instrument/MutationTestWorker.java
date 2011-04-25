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
package org.pitest.mutationtest.instrument;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.pitest.containers.UnContainer;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.extension.Transformation;
import org.pitest.functional.F2;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ConcreteResultCollector;
import org.pitest.internal.classloader.DefaultPITClassloader;
import org.pitest.mutationtest.ExitingResultCollector;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;
import org.pitest.mutationtest.loopbreak.LoopBreakTransformation;
import org.pitest.util.Log;

public class MutationTestWorker extends AbstractWorker {

  private final static Logger LOG = Log.getLogger();

  public MutationTestWorker(final F2<Class<?>, byte[], Boolean> hotswap,
      final Mutater mutater, final ClassLoader loader) {
    super(hotswap, mutater, loader);
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

    LOG.fine("mutating method " + mutatedClass.getDetails().getMethod());

    final List<TestUnit> relevantTests = testSource
        .translateTests(mutationDetails.getTestsInOrder());
    // pickTests(mutatedClass);

    r.describe(mutationId, relevantTests.size(), mutatedClass);

    DetectionStatus mutationDetected = DetectionStatus.SURVIVED;
    if ((relevantTests == null) || relevantTests.isEmpty()) {
      LOG.info("No test coverage for mutation  " + mutationId + " in "
          + mutatedClass.getDetails().getMethod());
    } else {
      LOG.info("" + relevantTests.size() + " relevant test for "
          + mutatedClass.getDetails().getMethod());

      final ClassLoader activeloader = pickClassLoaderForMutant(mutatedClass);
      final Container c = createNewContainer(activeloader);
      final Class<?> testee = Class.forName(mutationId.getClazz(), false,
          activeloader);

      final Transformation t = new LoopBreakTransformation();
      final long t0 = System.currentTimeMillis();
      if (this.hotswap.apply(testee,
          t.transform(mutationId.getClazz(), mutatedClass.getBytes()))) {
        LOG.fine("replaced class with mutant in "
            + (System.currentTimeMillis() - t0) + " ms");
        mutationDetected = doTestsDetectMutation(c, relevantTests);
      } else {
        LOG.info("Mutation " + mutationId + " was not viable ");
        mutationDetected = DetectionStatus.NON_VIABLE;
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
      return new DefaultPITClassloader(new ClassPath(), null);
    } else {
      return this.loader;
    }
  }

  private boolean hasMutationInStaticInitializer(final Mutant mutant) {
    return (mutant.getDetails().getId().isMutated())
        && mutant.getDetails().isInStaticInitializer();
  }

  @Override
  public String toString() {
    return "MutationTestWorker [mutater=" + this.mutater + ", loader="
        + this.loader + ", hotswap=" + this.hotswap + "]";
  }

}
