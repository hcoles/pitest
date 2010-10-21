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
package org.pitest.mutationtest.classloader;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.pitest.mutationtest.AbstractMutationTestUnit;
import org.pitest.mutationtest.CheckTestHasFailedResultListener;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.util.JavaProcess;

import com.reeltwo.jumble.mutation.Mutater;

public class MutationTestUnit extends AbstractMutationTestUnit {

  private static final Logger logger = Logger.getLogger(MutationTestUnit.class
                                         .getName());

  public MutationTestUnit(final Class<?> test, final Class<?> classToMutate,
      final MutationConfig mutationConfig, final Configuration pitConfig,
      final Description description) {
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

        final List<TestUnit> tests = findTestUnits();
        // m.setMutationPoint(0);
        final long normalExecution = timeUnmutatedTests(m
            .jumbler(this.classToMutate.getName()), tests, loader);
        final List<AssertionError> failures = new ArrayList<AssertionError>();

        // for (int i = 0; i != mutationCount; i++) {
        failures.addAll(runTestsInSeperateProcess(loader, mutationCount, tests,
            normalExecution));

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

  private int runTestInSeperateProcessForMutationRange(
      final Collection<AssertionError> results, final int start, final int end,
      final ClassPath cp, final List<TestUnit> tus, final long normalExecution)
      throws IOException {

    final File inputfile = File.createTempFile(randomFilename(), ".data");
    final File result = File.createTempFile(randomFilename(), ".results");

    final String[] args = createSlaveArgs(start, end, tus, normalExecution,
        inputfile, result, cp);

    final JavaProcess worker = JavaProcess.launch(Collections
        .<String> emptyList(), MutationTestSlave.class, Arrays.asList(args));

    try {
      worker.waitToDie();
      System.out.println("Worker has died");
    } catch (final InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    final int lastRunMutation = readResults(results, result);

    inputfile.delete();
    result.delete();

    return lastRunMutation;

  }

  private int readResults(final Collection<AssertionError> results,
      final File result) throws FileNotFoundException, IOException {
    final BufferedReader r = new BufferedReader(new InputStreamReader(
        new FileInputStream(result)));
    int lastRunMutation = -1;
    try {
      while (r.ready()) {
        final String line = r.readLine();
        final String[] parts = line.split(",");
        lastRunMutation = Integer.parseInt(parts[0].substring(0, parts[0]
            .indexOf("=")));
        if (parts[0].contains("false")) {
          results.add(arrayToAssertionError(parts));
        }
        System.out.println("Result from file " + line);
      }
    } finally {
      r.close();
    }
    return lastRunMutation;
  }

  private Collection<AssertionError> runTestsInSeperateProcess(
      final ClassLoader loader, final int mutationCount,
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
      lastRunMutation = runTestInSeperateProcessForMutationRange(results,
          start, end, cp, tus, normalExecution);
      start = lastRunMutation + 1;
    }

    return results;

  }

  private String[] createSlaveArgs(final int start, final int end,
      final List<TestUnit> tus, final long normalExecution,
      final File inputfile, final File result, final ClassPath cp)
      throws IOException {
    final BufferedWriter bw = new BufferedWriter(new FileWriter(inputfile));
    bw.append(IsolationUtils.toTransportString(this.config));
    bw.newLine();
    bw.append(IsolationUtils.toTransportString(cp));
    bw.newLine();
    bw.append(IsolationUtils.toTransportString(tus));
    bw.newLine();
    bw.close();
    final String[] args = createArgs(start, end, this.classToMutate,
        normalExecution, inputfile, result);
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
      final Class<?> clazz, final long normalExeution, final File input,
      final File output) {

    final String[] a = { "" + start, "" + end, clazz.getName(),
        "" + normalExeution, input.getAbsolutePath(), output.getAbsolutePath() };

    return a;
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

  private AssertionError arrayToAssertionError(final String[] parts) {
    final MutationDetails details = new MutationDetails(parts[1], parts[2],
        parts[3], parts[4]);
    return createAssertionError(details);

  }

  private AssertionError createAssertionError(final MutationDetails md) {
    final AssertionError ae = new AssertionError("The mutation -> " + md
        + " did not result in any test failures");
    final StackTraceElement[] stackTrace = { md.stackTraceDescription() };
    ae.setStackTrace(stackTrace);
    return ae;
  }

}
