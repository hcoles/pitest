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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassLoaderRepository;
import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.containers.UnContainer;
import org.pitest.coverage.calculator.CodeCoverageStore;
import org.pitest.coverage.calculator.InvokeEntry;
import org.pitest.coverage.calculator.InvokeQueue;
import org.pitest.coverage.calculator.InvokeStatistics;
import org.pitest.coverage.codeassist.CoverageTransformation;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.EmptyConfiguration;
import org.pitest.functional.F2;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ConcreteResultCollector;
import org.pitest.internal.classloader.DefaultPITClassloader;
import org.pitest.mutationtest.CheckTestHasFailedResultListener;
import org.pitest.mutationtest.ExitingResultCollector;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.util.ExitCodes;

import com.reeltwo.jumble.mutation.Mutater;

public class MutationTestWorker {

  private static final int     UNMUTATED = -1;
  private final List<TestUnit> tests;
  private final MutationConfig mutationConfig;
  private final ClassLoader    loader;

  public MutationTestWorker(final List<TestUnit> tests,
      final MutationConfig mutationConfig, final ClassLoader loader) {
    this.tests = tests;
    this.loader = loader;
    this.mutationConfig = mutationConfig;
  }

  public Statistics gatherStatistics(
      final F2<Class<?>, byte[], Boolean> hotswap, final String className,
      final Reporter r) throws ClassNotFoundException {
    final Mutater m = this.mutationConfig.createMutator();
    m.setRepository(new ClassLoaderRepository(this.loader));
    m.setMutationPoint(UNMUTATED);
    final JavaClass unmutated = m.jumbler(className);

    final InvokeStatistics invokeStatistics = new InvokeStatistics();
    final InvokeQueue invokeQueue = new InvokeQueue();
    CodeCoverageStore.init(invokeQueue, invokeStatistics);

    final Class<?> testee = Class.forName(className, false, this.loader);

    final CoverageTransformation cf = new CoverageTransformation();
    final byte[] instrumentedClass = cf.transform(className, unmutated
        .getBytes());

    hotswap.apply(testee, instrumentedClass);

    final Map<Integer, List<TestUnit>> stats = new HashMap<Integer, List<TestUnit>>();

    for (final TestUnit each : this.tests) {
      System.out.println("Gathering stats for " + each.description()
          + " out of " + this.tests.size());
      invokeStatistics.clearStats();
      final Set<Integer> lineVisits = getStatisticsForTest(hotswap, className,
          unmutated, each, invokeQueue, invokeStatistics);

      for (final Integer line : lineVisits) {
        List<TestUnit> coveringTests = stats.get(line);
        if (coveringTests == null) {
          coveringTests = new ArrayList<TestUnit>();
        }
        coveringTests.add(each);
        stats.put(line, coveringTests);

      }
    }

    hotswap.apply(testee, unmutated.getBytes());
    return new Statistics(stats);

  }

  private Set<Integer> getStatisticsForTest(
      final F2<Class<?>, byte[], Boolean> hotswap, final String className,
      final JavaClass unmutated, final TestUnit test,
      final InvokeQueue invokeQueue, final InvokeStatistics invokeStatistics)
      throws ClassNotFoundException {

    final Container c = new UnContainer();
    doTestsDetectMutation(c, Collections.singletonList(test));

    readStatisticsQueue(invokeStatistics, invokeQueue);

    final Set<Integer> lineVisits = invokeStatistics.getVisitedLines();
    return lineVisits;

  }

  private void readStatisticsQueue(final InvokeStatistics invokeStatistics,
      final InvokeQueue invokeQueue) {
    for (final InvokeEntry each : invokeQueue) {
      if (each != null) {
        switch (each.getType()) {
        case LINE: {
          invokeStatistics.visitLine(each.getClassId(), each.getCodeId());
          break;
        }
        case METHOD: {
          invokeStatistics.visitMethod(each.getClassId(), each.getCodeId());
          break;
        }
        }
      }
    }
  }

  protected int run(final F2<Class<?>, byte[], Boolean> hotswap,
      final int startMutation, final int endMutation, final String className,
      final Reporter r, final Statistics stats) throws IOException,
      ClassNotFoundException {

    System.out.println("Mutating class " + className);

    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    final PrintStream realOut = System.out;
    try {
      final PrintStream replacedOut = new PrintStream(bos);
      // System.setOut(replacedOut);
      return mutateAndRun(hotswap, startMutation, endMutation, className, r,
          realOut, stats);

    } finally {
      System.setOut(realOut);
    }

  }

  private int mutateAndRun(final F2<Class<?>, byte[], Boolean> hotswap,
      final int startMutation, final int endMutation, final String className,
      final Reporter r, final PrintStream realOut, final Statistics stats)
      throws ClassNotFoundException, IOException {

    final Mutater m = this.mutationConfig.createMutator();
    m.setRepository(new ClassLoaderRepository(this.loader));
    final int maxMutation = m.countMutationPoints(className);

    for (int i = startMutation; (i <= endMutation) && (i != maxMutation); i++) {
      realOut.println("Running mutation " + i);

      final JavaClass mutatedClass = getMutation(className, m, i);
      final String method = m.getMutatedMethodName(className);

      realOut.println("mutating method " + method);

      final List<TestUnit> relevantTests = pickTests(m, className, stats);

      boolean mutationDetected = false;
      if ((relevantTests == null) || relevantTests.isEmpty()) {
        realOut.println("No test coverage for mutation in " + method);
      } else {
        realOut.println("" + relevantTests.size() + " relevant test for "
            + method);

        final ClassLoader activeloader = pickClassLoaderForMethod(i, method);

        final Container c = new UnContainer() {
          @Override
          public void submit(final TestUnit group) {
            final ExitingResultCollector rc = new ExitingResultCollector(
                new ConcreteResultCollector(this.feedbackQueue));
            group.execute(activeloader, rc);
          }
        };

        final Class<?> testee = Class.forName(className, false, activeloader);

        if (hotswap.apply(testee, mutatedClass.getBytes())) {
          mutationDetected = doTestsDetectMutation(c, relevantTests);
        } else {
          realOut.println("Mutation " + i + " of " + endMutation
              + " was not viable ");
          mutationDetected = true;
        }

      }

      r.report(i, mutationDetected, mutatedClass, m, className);

      realOut.println("Mutation " + i + " of " + endMutation + " detected = "
          + mutationDetected);
    }

    realOut.println(".....................");

    return ExitCodes.OK;
  }

  private List<TestUnit> pickTests(final Mutater m, final String className,
      final Statistics stats) {

    // m.getModification()
    if (stats != null) {
      final String modification = m.getModification();
      final int lineNumber = MutationDetails.parseLineNumber(modification);
      return stats.getStats().get(lineNumber);
    } else {
      return this.tests;
    }
  }

  private ClassLoader pickClassLoaderForMethod(final int i, final String method) {
    if ((i != UNMUTATED) && method.trim().equals("<clinit>()V")) {
      System.out.println("Creating new classloader for static initializer");
      return new DefaultPITClassloader(new ClassPath(), null);
    } else {
      return this.loader;
    }
  }

  protected JavaClass getMutation(final String className, final Mutater m,
      final int i) throws ClassNotFoundException {

    m.setMutationPoint(i);
    final JavaClass mutatedClass = m.jumbler(className);
    return mutatedClass;
  }

  private static boolean doTestsDetectMutation(final Container c,
      final List<TestUnit> tests) {
    try {
      final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener();

      final EmptyConfiguration conf = new EmptyConfiguration();

      final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
      staticConfig.addTestListener(listener);

      final Pitest pit = new Pitest(staticConfig, conf);
      pit.run(c, tests);

      return listener.resultIndicatesSuccess();
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

}
