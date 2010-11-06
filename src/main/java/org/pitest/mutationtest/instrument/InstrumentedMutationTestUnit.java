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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.bcel.util.ClassLoaderRepository;
import org.pitest.Description;
import org.pitest.MetaData;
import org.pitest.extension.Configuration;
import org.pitest.extension.ResultCollector;
import org.pitest.extension.TestUnit;
import org.pitest.functional.F2;
import org.pitest.functional.Option;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.ClassPathRoot;
import org.pitest.internal.classloader.PITClassLoader;
import org.pitest.mutationtest.AbstractMutationTestUnit;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.instrument.ResultsReader.MutationResult;
import org.pitest.util.JavaAgent;
import org.pitest.util.JavaProcess;
import org.pitest.util.StreamMonitor;
import org.pitest.util.Unchecked;

import com.reeltwo.jumble.mutation.Mutater;

public class InstrumentedMutationTestUnit extends AbstractMutationTestUnit {

  private static JavaAgent    javaAgentJarFinder = new JavaAgentJarFinder();

  private static final Logger logger             = Logger
                                                     .getLogger(InstrumentedMutationTestUnit.class
                                                         .getName());

  private static final long   PROCESS_START_TIME = 5000;

  public InstrumentedMutationTestUnit(final Class<?> test,
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

    try {
      if (mutationCount > 0) {
        // should test unit perhaps have PitClassloader in it's interface?
        final ClassPath cp = createClassPath(loader);

        final List<TestUnit> tests = findTestUnits();

        if (!tests.isEmpty()) {

          // run original tests in this process. If we are distributed
          // this will suck required classes and resources to cache
          // if we are not distributed this is of limited value . . .
          final long normalExecution = runUnmutatedTests(tests);

          final Map<Integer, MutationResult> mutations = runTestsInSeperateProcess(
              cp, mutationCount, tests, normalExecution);

          reportResults(mutationCount, mutations, rc);

        } else {
          rc.notifyEnd(this.description(), new AssertionError(
              "No tests to mutation test"));
        }

      } else {
        logger.info("Skipping test " + this.description()
            + " as no mutations found");
        rc.notifySkipped(this.description());
      }
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private long runUnmutatedTests(final List<TestUnit> tests) throws IOException {

    final long t0 = System.currentTimeMillis();
    final MutationTestWorker worker = new MutationTestWorker(tests,
        this.config, IsolationUtils.getContextClassLoader());

    final CheckForFailureReporter r = new CheckForFailureReporter();
    try {
      final F2<Class<?>, byte[], Boolean> nullHotSwap = new F2<Class<?>, byte[], Boolean>() {
        public Boolean apply(final Class<?> a, final byte[] b) {
          return true;
        }

      };

      worker.run(nullHotSwap, -1, -1, this.classToMutate.getName(), r, null);
    } catch (final ClassNotFoundException ex) {
      throw Unchecked.translateCheckedException(ex);
    }

    final long normalExecution = System.currentTimeMillis() - t0;

    if (r.hadFailure()) {
      System.out.println("Tests do not run green when no mutation present");
      throw new RuntimeException(
          "Cannot mutation test as tests do not pass without mutation");
    }
    return normalExecution;
  }

  private SlaveResult runTestInSeperateProcessForMutationRange(
      final Map<Integer, MutationResult> mutations, final int start,
      final int end, final ClassPath cp, final List<TestUnit> tus,
      final long normalExecution, final Option<Statistics> stats)
      throws IOException {

    final File inputfile = File.createTempFile(randomFilename(), ".data");
    final File result = File.createTempFile(randomFilename(), ".results");

    final String[] args = createSlaveArgs(start, end, tus, inputfile, result,
        stats);

    final String lauchClassPath = getLaunchClassPath(cp);

    final JavaProcess worker = JavaProcess.launch(Collections
        .<String> emptyList(), InstrumentedMutationTestSlave.class, Arrays
        .asList(args), javaAgentJarFinder, lauchClassPath);

    boolean timedOut = false;
    final Thread t = createWorkerThread(worker);

    final InputStream fis = new BufferedInputStream(new FileInputStream(result));
    final ResultsReader rr = new ResultsReader(end, mutations, stats);
    final StreamMonitor sm = new StreamMonitor(fis, rr);

    try {
      if (normalExecution != 0) {
        t.start();
        final long timeout = PROCESS_START_TIME + (normalExecution + 5)
            * (end - start + 1);
        System.out.println("Timeout is " + timeout + " for " + start + " to "
            + end + ". normal exec was " + normalExecution);
        t.join(timeout);
        if (worker.isAlive()) {
          timedOut = true;
        }
        worker.destroy();
      } else {
        t.run();
      }
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }

    sm.requestStop();

    try {
      // thread might be blocked with no input to read
      if (!timedOut) {
        sm.join(5000);
      }
    } catch (final InterruptedException e) {
      // ignore
    }

    final SlaveResult sr = rr.getResult();
    int lastRunMutation = sr.getLastRunMutation();
    if (timedOut) {
      mutations.get(lastRunMutation).detected = true;
      // skip one as result cannot have been written if an infinite loop
      // occurred for the mutation
      lastRunMutation = lastRunMutation + 1;
      System.out.println("Timed out while running mutation " + lastRunMutation);
    }

    fis.close();
    inputfile.delete();
    result.delete();

    return new SlaveResult(lastRunMutation, sr.getStats());

  }

  private Thread createWorkerThread(final JavaProcess worker) {
    final Thread t = new Thread() {
      @Override
      public void run() {
        try {
          final int exitCode = worker.waitToDie();
          System.out.println("Exit code was " + exitCode);
        } catch (final InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };

    t.setDaemon(true);
    return t;
  }

  private String getLaunchClassPath(final ClassPath cp) {
    String classpath = System.getProperty("java.class.path");
    for (final ClassPathRoot each : cp) {
      final Option<String> additional = each.cacheLocation();
      for (final String path : additional) {
        classpath = classpath + File.pathSeparator + path;
      }
    }
    return classpath;
  }

  private Map<Integer, MutationResult> runTestsInSeperateProcess(
      final ClassPath cp, final int mutationCount, final List<TestUnit> tus,
      final long normalExecution) throws IOException, InterruptedException {

    final Map<Integer, MutationResult> mutations = new HashMap<Integer, MutationResult>();
    int start = 0;
    final int end = mutationCount;
    Option<Statistics> stats = Option.none();

    while (start < mutationCount) {
      System.out.println("Testing mutations " + start + " to " + end + " of "
          + mutationCount);
      final SlaveResult sr = runTestInSeperateProcessForMutationRange(
          mutations, start, end, cp, tus, normalExecution, stats);
      start = sr.getLastRunMutation() + 1;
      stats = sr.getStats();
      System.out.println("Got stats from slave " + stats);
    }

    return mutations;

  }

  private String[] createSlaveArgs(final int start, final int end,
      final List<TestUnit> tus, final File inputfile, final File result,
      final Option<Statistics> stats) throws IOException {
    final BufferedWriter bw = new BufferedWriter(new FileWriter(inputfile));
    bw.append(IsolationUtils.toTransportString(stats));
    bw.newLine();
    bw.append(IsolationUtils.toTransportString(this.config));
    bw.newLine();
    bw.append(IsolationUtils.toTransportString(tus));
    bw.newLine();
    bw.close();
    final String[] args = createArgs(start, end, this.classToMutate, inputfile,
        result);
    return args;
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

  private String[] createArgs(final int start, final int end,
      final Class<?> clazz, final File input, final File output) {

    final String[] a = { "" + start, "" + end, clazz.getName(),
        input.getAbsolutePath(), output.getAbsolutePath() };

    return a;
  }

  private void reportResults(final int mutationCount,
      final Map<Integer, MutationResult> mutations, final ResultCollector rc) {

    final List<AssertionError> failures = new ArrayList<AssertionError>();
    final MetaData md = new MutationMetaData(mutations.values());
    for (final MutationResult result : mutations.values()) {
      if (!result.detected) {
        failures.add(createAssertionError(result.details));
      }
    }

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
      rc.notifyEnd(this.description(), ae, md);
    } else {
      rc.notifyEnd(this.description(), md);
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
