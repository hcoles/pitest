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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassLoaderRepository;
import org.pitest.Description;
import org.pitest.extension.Configuration;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F3;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.PITClassLoader;
import org.pitest.util.Debugger;
import org.pitest.util.DefaultDebugger;
import org.pitest.util.Unchecked;

import com.reeltwo.jumble.mutation.Mutater;

public class HotSwapMutationTestUnit extends AbstractMutationTestUnit {

  // private static HotSwapWorker worker;
  private final static ThreadLocal<HotSwapWorker> threadWorker = new ThreadLocal<HotSwapWorker>();

  private static final Logger                     logger       = Logger
                                                                   .getLogger(HotSwapMutationTestUnit.class
                                                                       .getName());

  public HotSwapMutationTestUnit(final Class<?> test,
      final Class<?> classToMutate, final MutationConfig mutationConfig,
      final Configuration pitConfig, final Description description) {
    super(test, classToMutate, mutationConfig, pitConfig, description);

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

  private void runTests(final ResultCollector rc, final ClassLoader loader) {

    final Mutater m = this.config.createMutator();
    m.setRepository(new ClassLoaderRepository(loader));
    final String name = this.classToMutate.getName();

    final int mutationCount = m.countMutationPoints(name);

    // missing things
    // run unmutated test in process
    // timeouts
    // actually report on the mutation

    try {
      if (mutationCount > 0) {

        final List<TestUnit> tests = findTestUnits();
        // m.setMutationPoint(0);
        final long normalExecution = timeUnmutatedTests(tests, m, loader);
        final List<AssertionError> failures = new ArrayList<AssertionError>();

        failures.addAll(runTestsInSeperateProcess(loader, m, mutationCount,
            tests, normalExecution));

        reportResults(mutationCount, failures, rc);

      } else {
        logger.info("Skipping test " + this.description()
            + " as no mutations found");
        rc.notifySkipped(this.description());
      }
    } catch (final Exception ex) {
      ex.printStackTrace();
      throw translateCheckedException(ex);
    }

  }

  public static String randomFilename() {
    return System.currentTimeMillis()
        + ("" + Math.random()).replaceAll("\\.", "");
  }

  private synchronized HotSwapWorker getWorker(final Debugger debugger,
      final ClassPath cp) throws IOException {
    final HotSwapWorker current = threadWorker.get();
    if ((current == null) || current.getProcess().isAlive()) {
      System.out.println(">>>>>>> Creating new worker for thread "
          + Thread.currentThread());
      final HotSwapWorker w = new HotSwapWorker(debugger, cp);
      threadWorker.set(w);
      if (!w.waitForRunToComplete(60 * 1000)) {
        w.getProcess().destroy();
        throw new RuntimeException("Worker failed to respond");
      }

    }
    return threadWorker.get();
  }

  private int runTestInSeperateProcessForMutationRange(
      final Mutater m,
      final Collection<AssertionError> results,
      final int start,
      final int end,
      final ClassPath cp,
      final List<TestUnit> tus,
      final long normalExecution,
      final F3<Mutater, Collection<AssertionError>, File, Integer> reportFunction)
      throws IOException {

    final HotSwapWorker worker = this.getWorker(new DefaultDebugger(), cp);
    createInputFile(start, end, tus, worker.getInputfile());

    worker.reset(this.classToMutate, m, start, end);

    worker.getDebugger().resume();

    final long timeout = (normalExecution + 100) * (end - start + 1);
    final boolean timedOut = !worker.waitForRunToComplete(timeout)
        && !(normalExecution == 0);

    System.out.println("Worker has finished. TimedOut = " + timedOut);
    worker.getInputfile().delete(); // will trigger IOException and exit in
                                    // slave
    if (timedOut) {
      worker.getProcess().destroy();
      threadWorker.set(null);
    }

    final int lastRunMutation = reportFunction.apply(m, results, worker
        .getResult());
    // readResults(m, results, worker.getResult());

    if (timedOut) {
      return lastRunMutation + 1;
    } else {
      return lastRunMutation;
    }

  }

  private int readResults(final Mutater m,
      final Collection<AssertionError> results, final File result) {
    BufferedReader r = null;
    int lastRunMutation = -1;
    try {
      r = new BufferedReader(new InputStreamReader(new FileInputStream(result)));
      while (r.ready()) {
        final String line = r.readLine();
        final String parts[] = line.split("=");
        lastRunMutation = Integer.parseInt(parts[0]);
        if (parts[1].contains("false")) {
          final String className = this.classToMutate.getName();
          m.setMutationPoint(lastRunMutation);
          final JavaClass mutatedClass = m.jumbler(className);

          final MutationDetails details = new MutationDetails(
              this.classToMutate.getName(), mutatedClass.getFileName(), m
                  .getModification(), m.getMutatedMethodName(className));
          results.add(createAssertionError(details));

        }
        System.out.println("Result from file " + line);
      }
    } catch (final ClassNotFoundException e) {
      throw Unchecked.translateCheckedException(e);
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    } finally {
      try {
        r.close();
      } catch (final IOException e) {
        throw Unchecked.translateCheckedException(e);
      }
    }
    return lastRunMutation;
  }

  private Collection<AssertionError> runTestsInSeperateProcess(
      final ClassLoader loader, final Mutater m, final int mutationCount,
      final List<TestUnit> tus, final long normalExecution) throws IOException,
      InterruptedException {

    final Collection<AssertionError> results = new ArrayList<AssertionError>();
    // should test unit perhaps have PitClassloader in it's interface?
    final ClassPath cp = createClassPath(loader);

    int start = 0;
    final int end = mutationCount;
    int lastRunMutation = -1;

    final F3<Mutater, Collection<AssertionError>, File, Integer> reporter = new F3<Mutater, Collection<AssertionError>, File, Integer>() {

      public Integer apply(final Mutater a, final Collection<AssertionError> b,
          final File c) {
        return readResults(a, b, c);
      }

    };

    while (start != mutationCount) {
      System.out.println("Testing mutations " + start + " to " + end + " of "
          + mutationCount);

      lastRunMutation = runTestInSeperateProcessForMutationRange(m, results,
          start, end, cp, tus, normalExecution, reporter);
      start = lastRunMutation + 1;
    }

    resetClassToUnmutatedState(m);

    return results;

  }

  private void resetClassToUnmutatedState(final Mutater m) {
    try {
      final HotSwapWorker current = threadWorker.get();
      if (current != null) {
        m.setMutationPoint(-1);
        current.getDebugger().hotSwapClass(
            m.jumbler(this.classToMutate.getName()).getBytes(),
            this.classToMutate.getName());
      }
    } catch (final ClassNotFoundException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

  private void createInputFile(final int start, final int end,
      final List<TestUnit> tus, final File inputfile) throws IOException {
    final BufferedWriter bw = new BufferedWriter(new FileWriter(inputfile,
        false));
    final RunDetails rd = new RunDetails();
    rd.setClassName(this.classToMutate.getName());
    rd.setEndMutation(end);
    System.out.println("Writing input file staritng at " + start);
    rd.setStartMutation(start);
    rd.setTests(tus);
    bw.append(IsolationUtils.toTransportString(rd));
    bw.newLine();
    bw.close();

  }

  private ClassPath createClassPath(final ClassLoader loader) {
    ClassPath cp = null;
    if (loader instanceof PITClassLoader) {
      cp = ((PITClassLoader) loader).getClassPath();
    } else {
      cp = new ClassPath();
    }
    return cp;
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

  private long timeUnmutatedTests(final List<TestUnit> list, final Mutater m,
      final ClassLoader loader) throws IOException {
    final Collection<AssertionError> results = new ArrayList<AssertionError>();
    final ClassPath cp = createClassPath(loader);
    final long t0 = System.currentTimeMillis();
    final F3<Mutater, Collection<AssertionError>, File, Integer> reportFunction = new F3<Mutater, Collection<AssertionError>, File, Integer>() {

      public Integer apply(final Mutater m,
          final Collection<AssertionError> results, final File result) {
        BufferedReader r = null;
        final int lastRunMutation = -1;
        try {
          r = new BufferedReader(new InputStreamReader(new FileInputStream(
              result)));
          while (r.ready()) {
            final String line = r.readLine();
            if (line.contains("true")) {
              results
                  .add(new AssertionError("Test failed with unmutated class"));
            }
            System.out.println("Result from file " + line);
          }
        } catch (final IOException e) {
          throw Unchecked.translateCheckedException(e);
        } finally {
          try {
            r.close();
          } catch (final IOException e) {
            throw Unchecked.translateCheckedException(e);
          }
        }
        return lastRunMutation;
      }

    };

    runTestInSeperateProcessForMutationRange(m, results, -1, 0, cp, list, 0,
        reportFunction);
    if (!results.isEmpty()) {
      throw new RuntimeException(
          "Cannot mutation test as tests do not pass without mutation");
    }
    return System.currentTimeMillis() - t0;
  }

  private AssertionError createAssertionError(final MutationDetails md) {
    final AssertionError ae = new AssertionError("The mutation -> " + md
        + " did not result in any test failures");
    final StackTraceElement[] stackTrace = { md.stackTraceDescription() };
    ae.setStackTrace(stackTrace);
    return ae;
  }

}
