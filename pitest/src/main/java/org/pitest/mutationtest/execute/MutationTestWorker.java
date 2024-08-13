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

import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.environment.ResetEnvironment;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.mocksupport.JavassistInterceptor;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.execute.Container;
import org.pitest.testapi.execute.ExitingResultCollector;
import org.pitest.testapi.execute.MultipleTestGroup;
import org.pitest.testapi.execute.Pitest;
import org.pitest.testapi.execute.containers.ConcreteResultCollector;
import org.pitest.testapi.execute.containers.UnContainer;
import org.pitest.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.pitest.util.Unchecked.translateCheckedException;

public class MutationTestWorker {

  private static final Logger                               LOG   = Log
      .getLogger();

  // micro optimise debug logging
  private static final boolean                              DEBUG = LOG
      .isLoggable(Level.FINE);

  private final Mutater                                     mutater;
  private final ClassLoader                                 loader;
  private final HotSwap                                     hotswap;
  private final boolean                                     fullMutationMatrix;

  private final ResetEnvironment                            reset;

  public MutationTestWorker(HotSwap hotswap,
                            Mutater mutater,
                            ClassLoader loader,
                            ResetEnvironment reset,
                            boolean fullMutationMatrix) {
    this.loader = loader;
    this.reset = reset;
    this.mutater = mutater;
    this.hotswap = hotswap;
    this.fullMutationMatrix = fullMutationMatrix;
  }

  protected void run(final Collection<MutationDetails> range, final Reporter r,
      final TimeOutDecoratedTestSource testSource) throws IOException {

    for (final MutationDetails mutation : range) {
      if (DEBUG) {
        LOG.fine("Running mutation " + mutation);
      }
      final long t0 = System.nanoTime();
      processMutation(r, testSource, mutation);
      if (DEBUG) {
        LOG.fine("processed mutation in " + NANOSECONDS.toMillis(System.nanoTime() - t0)
            + " ms.");
      }
    }

  }

  private void processMutation(final Reporter r,
      final TimeOutDecoratedTestSource testSource,
      final MutationDetails mutationDetails) {

    final MutationIdentifier mutationId = mutationDetails.getId();
    final Mutant mutatedClass = this.mutater.getMutation(mutationId);

    // For the benefit of mocking frameworks such as PowerMock
    // mess with the internals of Javassist so our mutated class
    // bytes are returned
    JavassistInterceptor.setMutant(mutatedClass);
    reset.resetFor(mutatedClass);

    if (DEBUG) {
      LOG.fine("mutating method " + mutatedClass.getDetails().getMethod());
    }
    final List<TestUnit> relevantTests = testSource
        .translateTests(mutationDetails.getTestsInOrder());

    r.describe(mutationId);

    final MutationStatusTestPair mutationDetected = handleMutation(
        mutationDetails, mutatedClass, relevantTests);

    r.report(mutationId, mutationDetected);
    if (DEBUG) {
      LOG.fine("Mutation " + mutationId + " detected = " + mutationDetected);
    }
  }

  private MutationStatusTestPair handleMutation(
      final MutationDetails mutationId, final Mutant mutatedClass,
      final List<TestUnit> relevantTests) {
    final MutationStatusTestPair mutationDetected;
    if ((relevantTests == null) || relevantTests.isEmpty()) {
      LOG.info(() -> "No test coverage for mutation " + mutationId + " in "
          + mutatedClass.getDetails().getMethod());
      mutationDetected =  MutationStatusTestPair.notAnalysed(0, DetectionStatus.RUN_ERROR, Collections.emptyList());
    } else {
      mutationDetected = handleCoveredMutation(mutationId, mutatedClass,
          relevantTests);

    }
    return mutationDetected;
  }

  private MutationStatusTestPair handleCoveredMutation(
      final MutationDetails mutationId, final Mutant mutatedClass,
      final List<TestUnit> relevantTests) {
    final MutationStatusTestPair mutationDetected;
    if (DEBUG) {
      LOG.fine("" + relevantTests.size() + " relevant test for "
          + mutatedClass.getDetails().getMethod());
    }

    final Container c = createNewContainer();
    final long t0 = System.nanoTime();

    if (this.hotswap.insertClass(mutationId.getClassName(), this.loader,
        mutatedClass.getBytes())) {
      if (DEBUG) {
        LOG.fine("replaced class with mutant in "
            + NANOSECONDS.toMillis(System.nanoTime() - t0) + " ms");
      }

      mutationDetected = doTestsDetectMutation(c, relevantTests);
    } else {
      LOG.warning("Mutation " + mutationId + " was not viable ");
      mutationDetected = MutationStatusTestPair.notAnalysed(0,
          DetectionStatus.NON_VIABLE, relevantTests.stream()
              .map(t -> t.getDescription().getQualifiedName())
              .collect(Collectors.toList()));
    }
    return mutationDetected;
  }

  private static Container createNewContainer() {
    return new UnContainer() {
      @Override
      public List<TestResult> execute(final TestUnit group) {
        final Collection<TestResult> results = new ConcurrentLinkedDeque<>();
        final ExitingResultCollector rc = new ExitingResultCollector(
            new ConcreteResultCollector(results));
        group.execute(rc);
        return new ArrayList<>(results);
      }
    };
  }



  @Override
  public String toString() {
    return "MutationTestWorker [mutater=" + this.mutater + ", loader="
        + this.loader + ", hotswap=" + this.hotswap + "]";
  }

  private MutationStatusTestPair doTestsDetectMutation(final Container c,
      final List<TestUnit> tests) {
    try {
      final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener(fullMutationMatrix);

      final Pitest pit = new Pitest(listener);

      if (this.fullMutationMatrix) {
        pit.run(c, tests);
      } else {
        pit.run(c, createEarlyExitTestGroup(tests));
      }

      return createStatusTestPair(listener, tests);
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private MutationStatusTestPair createStatusTestPair(
      final CheckTestHasFailedResultListener listener, List<TestUnit> relevantTests) {
    List<String> failingTests = listener.getFailingTests().stream()
        .map(Description::getQualifiedName).collect(Collectors.toList());
    List<String> succeedingTests = listener.getSucceedingTests().stream()
        .map(Description::getQualifiedName).collect(Collectors.toList());
    List<String> coveredTests = relevantTests.stream()
        .map(t -> t.getDescription().getQualifiedName()).collect(Collectors.toList());

    return new MutationStatusTestPair(listener.getNumberOfTestsRun(),
        listener.status(), failingTests, succeedingTests, coveredTests);
  }

  private List<TestUnit> createEarlyExitTestGroup(final List<TestUnit> tests) {
    return Collections.singletonList(new MultipleTestGroup(tests));
  }

}
