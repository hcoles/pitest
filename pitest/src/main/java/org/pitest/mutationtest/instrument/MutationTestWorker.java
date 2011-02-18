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

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.containers.UnContainer;
import org.pitest.coverage.CodeCoverageStore;
import org.pitest.coverage.CoverageStatistics;
import org.pitest.coverage.InvokeQueue;
import org.pitest.coverage.codeassist.CoverageTransformation;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.extension.Transformation;
import org.pitest.extension.common.CompoundTransformation;
import org.pitest.extension.common.EmptyConfiguration;
import org.pitest.functional.F;
import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ConcreteResultCollector;
import org.pitest.internal.classloader.DefaultPITClassloader;
import org.pitest.internal.transformation.IdentityTransformation;
import org.pitest.mutationtest.CheckTestHasFailedResultListener;
import org.pitest.mutationtest.ExitingResultCollector;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;
import org.pitest.mutationtest.loopbreak.LoopBreakTransformation;
import org.pitest.mutationtest.loopbreak.PerProcessTimelimitCheck;
import org.pitest.util.Monitor;
import org.pitest.util.NullMonitor;
import org.pitest.util.Unchecked;

public class MutationTestWorker {

  private final List<TestUnit>                tests;
  private final Mutater                       mutater;
  private final ClassLoader                   loader;
  private final F2<Class<?>, byte[], Boolean> hotswap;

  public MutationTestWorker(final F2<Class<?>, byte[], Boolean> hotswap,
      final List<TestUnit> tests, final Mutater mutater,
      final ClassLoader loader) {
    this.tests = tests;
    this.loader = loader;
    this.mutater = mutater;
    this.hotswap = hotswap;
  }

  public Statistics gatherStatistics(final Collection<String> classNames,
      final Reporter r) {

    final FunctionalList<Mutant> unmutatedClasses = FCollection.map(classNames,
        nameToUnmodifiedClass());

    final CoverageStatistics invokeStatistics = new CoverageStatistics();
    final InvokeQueue invokeQueue = new InvokeQueue();
    CodeCoverageStore.init(invokeQueue, invokeStatistics);
    instrumentClassesForCodeCoverage(unmutatedClasses);

    final Map<ClassLine, List<TestUnit>> lineToTestUnitsMap = new HashMap<ClassLine, List<TestUnit>>();

    final List<TestUnit> decoratedTests = decorateForCoverage(this.tests,
        invokeStatistics, invokeQueue, lineToTestUnitsMap, classNames);

    final Container c = new UnContainer();

    final DetectionStatus status = doTestsDetectMutation(c, decoratedTests);

    removeInstrumentationFromClasses(unmutatedClasses);

    final Map<TestUnit, Long> executionTimes = new HashMap<TestUnit, Long>();
    for (final TestUnit each : decoratedTests) {
      final CoverageDecorator cd = (CoverageDecorator) each;
      executionTimes.put(cd.child(), cd.getExecutionTime());
    }

    return new Statistics(!status.isDetected(), executionTimes,
        lineToTestUnitsMap);
  }

  private void instrumentClassesForCodeCoverage(
      final FunctionalList<Mutant> unmutatedClasses) {
    final CoverageTransformation cf = new CoverageTransformation();
    // include loopbreaking as it may affect timing
    final Transformation transformForLineCoverage = new CompoundTransformation(
        cf, new LoopBreakTransformation());
    PerProcessTimelimitCheck.disableLoopBreaking();
    unmutatedClasses
        .forEach(hotswapClassWithTransformation(transformForLineCoverage));
  }

  private void removeInstrumentationFromClasses(
      final FunctionalList<Mutant> unmutatedClasses) {
    final IdentityTransformation backToUnmutated = new IdentityTransformation();
    unmutatedClasses.forEach(hotswapClassWithTransformation(backToUnmutated));
  }

  private SideEffect1<Mutant> hotswapClassWithTransformation(
      final Transformation t) {
    return new SideEffect1<Mutant>() {

      public void apply(final Mutant mutant) {
        try {
          final Class<?> testee = Class.forName(mutant.getDetails().getClazz(),
              false, MutationTestWorker.this.loader);
          final byte[] instrumentedClass = t.transform(mutant.getDetails()
              .getClazz(), mutant.getBytes());
          MutationTestWorker.this.hotswap.apply(testee, instrumentedClass);
        } catch (final ClassNotFoundException ex) {
          throw Unchecked.translateCheckedException(ex);
        }
      }

    };
  }

  private F<String, Mutant> nameToUnmodifiedClass() {
    return new F<String, Mutant>() {

      public Mutant apply(final String clazz) {
        return MutationTestWorker.this.mutater.getUnmodifiedClass(clazz);
      }

    };
  }

  private List<TestUnit> decorateForCoverage(final List<TestUnit> plainTests,
      final CoverageStatistics stats, final InvokeQueue queue,
      final Map<ClassLine, List<TestUnit>> lineMapping,
      final Collection<String> classNames) {
    final List<TestUnit> decorated = new ArrayList<TestUnit>(plainTests.size());
    for (final TestUnit each : plainTests) {
      decorated.add(new CoverageDecorator(classNames, queue, stats,
          lineMapping, each));
    }
    return decorated;
  }

  protected void run(final Collection<MutationIdentifier> range,
      final Reporter r, final Statistics stats, final boolean useTimeOut)
      throws IOException, ClassNotFoundException {

    // System.out.println("Mutating class " + classesToMutate);

    for (final MutationIdentifier i : range) {
      System.out.println("Running mutation " + i);

      final Mutant mutatedClass = this.mutater.getMutation(i);

      System.out.println("mutating method "
          + mutatedClass.getDetails().getMethod());

      final List<TestUnit> relevantTests = pickTests(mutatedClass, stats);

      r.describe(i, relevantTests.size(), mutatedClass);

      DetectionStatus mutationDetected = DetectionStatus.SURVIVED;
      if ((relevantTests == null) || relevantTests.isEmpty()) {
        System.out.println("No test coverage for mutation  " + i + " in "
            + mutatedClass.getDetails().getMethod());
      } else {
        System.out.println("" + relevantTests.size() + " relevant test for "
            + mutatedClass.getDetails().getMethod());

        final ClassLoader activeloader = pickClassLoaderForMutant(mutatedClass);
        final Container c = createNewContainer(activeloader);
        final Class<?> testee = Class
            .forName(i.getClazz(), false, activeloader);

        final Transformation t = new LoopBreakTransformation();
        if (this.hotswap.apply(testee,
            t.transform(i.getClazz(), mutatedClass.getBytes()))) {

          Monitor timeoutWatchDog = null;
          try {
            timeoutWatchDog = setupTimeOuts(stats, useTimeOut, relevantTests);
            mutationDetected = doTestsDetectMutation(c, relevantTests);
          } finally {
            timeoutWatchDog.requestStop();
          }

        } else {
          System.out.println("Mutation " + i + " of " + range.size()
              + " was not viable ");
          mutationDetected = DetectionStatus.NON_VIABLE;
        }

      }

      r.report(i, mutationDetected);

      System.out.println("Mutation " + i + " of " + range.size()
          + " detected = " + mutationDetected);
    }

    System.out.println(".....................");

  }

  private Monitor setupTimeOuts(final Statistics stats,
      final boolean useTimeOut, final List<TestUnit> relevantTests) {
    Monitor timeoutWatchDog;
    // FIXME why not make this finer grained with a decorator?
    // NOTE the watchdog will prevent reporting of earlier tests
    // so non timing out tests will be timed out . . .
    if (useTimeOut) {
      final long loopBreakTimeout = stats.getExecutionTime(relevantTests) + 1000;
      PerProcessTimelimitCheck.setMaxEndTime(System.currentTimeMillis()
          + loopBreakTimeout);
      // PerProcessTimelimitCheck.disableLoopBreaking();
      timeoutWatchDog = new TimeoutWatchDog(loopBreakTimeout + 1000);
    } else {
      PerProcessTimelimitCheck.disableLoopBreaking();
      timeoutWatchDog = new NullMonitor();
    }

    timeoutWatchDog.requestStart();
    return timeoutWatchDog;
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

  private List<TestUnit> pickTests(final Mutant m, final Statistics stats) {
    if (stats.hasCoverageData() && !hasMutationInStaticInitializer(m)) {
      System.out.println("Picking tests");
      return stats.getTestForLineNumber(m.getDetails().getClassLine());
    } else {
      System.out.println("Returning all tests");
      return this.tests;
    }
  }

  private ClassLoader pickClassLoaderForMutant(final Mutant mutant) {
    if (hasMutationInStaticInitializer(mutant)) {
      System.out.println("Creating new classloader for static initializer");
      return new DefaultPITClassloader(new ClassPath(), null);
    } else {
      return this.loader;
    }
  }

  private boolean hasMutationInStaticInitializer(final Mutant mutant) {
    return (mutant.getDetails().getId().isMutated())
        && mutant.getDetails().isInStaticInitializer();
  }

  private DetectionStatus doTestsDetectMutation(final Container c,
      final List<TestUnit> tests) {
    try {
      final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener();

      final EmptyConfiguration conf = new EmptyConfiguration();

      final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
      staticConfig.addTestListener(listener);

      final Pitest pit = new Pitest(staticConfig, conf);
      pit.run(c, tests);

      return listener.status();
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

}
