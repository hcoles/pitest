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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.util.ClassLoaderRepository;
import org.pitest.DefaultStaticConfig;
import org.pitest.Description;
import org.pitest.Pitest;
import org.pitest.extension.Configuration;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.EmptyConfiguration;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.OtherClassLoaderClassPathRoot;
import org.pitest.internal.classloader.PITClassLoader;
import org.pitest.util.Debugger;
import org.pitest.util.DefaultDebugger;
import org.pitest.util.Unchecked;

import com.reeltwo.jumble.mutation.Mutater;

public class HotSwapMutationTestUnit extends AbstractMutationTestUnit {

  // private static HotSwapWorker worker;
  private final static ThreadLocal<HotSwapWorker> worker = new ThreadLocal<HotSwapWorker>();

  private static final Logger                     logger = Logger
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
        final long normalExecution = timeUnmutatedTests(m
            .jumbler(this.classToMutate.getName()), tests, loader);
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
    final HotSwapWorker current = worker.get();
    if (current == null) {
      System.out.println(">>>>>>> Creating new worker for thread "
          + Thread.currentThread());
      worker.set(new HotSwapWorker(debugger, cp));
    }
    return worker.get();
  }

  private int runTestInSeperateProcessForMutationRange(final Mutater m,
      final Collection<AssertionError> results, final int start, final int end,
      final ClassPath cp, final List<TestUnit> tus, final long normalExecution)
      throws IOException {

    final HotSwapWorker worker = this.getWorker(new DefaultDebugger(), cp);

    createInputFile(start, end, tus, normalExecution, worker.getInputfile());
    worker.reset(this.classToMutate, m, start);
    worker.getDebugger().resume();

    try {
      while (!worker.isRunComplete()) {
        Thread.sleep(500);
      }
      System.out.println("Worker has finished");
      worker.getInputfile().delete(); // will trigger IOException and exit in
      // slave when controlling thread dies
    } catch (final InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    final int lastRunMutation = readResults(m, results, worker.getResult());

    return lastRunMutation;

  }

  private int readResults(final Mutater m,
      final Collection<AssertionError> results, final File result)
      throws FileNotFoundException, IOException {
    final BufferedReader r = new BufferedReader(new InputStreamReader(
        new FileInputStream(result)));
    int lastRunMutation = -1;
    try {
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
    } finally {
      r.close();
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

    while (start != mutationCount) {
      System.out.println("Testing mutations " + start + " to " + end + " of "
          + mutationCount);
      lastRunMutation = runTestInSeperateProcessForMutationRange(m, results,
          start, end, cp, tus, normalExecution);
      start = lastRunMutation + 1;
    }

    resetClassToUnmutatedState(m);

    return results;

  }

  private void resetClassToUnmutatedState(final Mutater m) {
    try {
      final HotSwapWorker current = worker.get();
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
      final List<TestUnit> tus, final long normalExecution, final File inputfile)
      throws IOException {
    final BufferedWriter bw = new BufferedWriter(new FileWriter(inputfile,
        false));
    final RunDetails rd = new RunDetails();
    rd.setClassName(this.classToMutate.getName());
    rd.setEndMutation(end);
    rd.setStartMutation(start);
    rd.setNormalExecutionTime(normalExecution);
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

  private long timeUnmutatedTests(final JavaClass unmutatedClass,
      final List<TestUnit> list, final ClassLoader loader) {
    final long t0 = System.currentTimeMillis();
    if (doTestsDetectMutation(loader, unmutatedClass, list, -1)) {
      throw new RuntimeException(
          "Cannot mutation test as tests do not pass without mutation");
    }
    return System.currentTimeMillis() - t0;
  }

  private boolean doTestsDetectMutation(final ClassLoader loader,
      final JavaClass mutatedClass, final List<TestUnit> tests,
      final long normalExecutionTime) {
    try {
      final CheckTestHasFailedResultListener listener = new CheckTestHasFailedResultListener();
      final ClassPath classPath = new ClassPath(
          new OtherClassLoaderClassPathRoot(loader));

      final JumbleContainer c = new JumbleContainer(classPath, mutatedClass,
          normalExecutionTime);

      final EmptyConfiguration conf = new EmptyConfiguration();
      final Pitest pit = new Pitest(conf);
      final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
      staticConfig.addTestListener(listener);
      pit.run(c, staticConfig, tests);

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

}
